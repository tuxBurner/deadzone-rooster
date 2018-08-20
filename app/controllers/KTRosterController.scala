package controllers

import java.util.UUID

import com.github.tuxBurner.jsAnnotations.JSRoute
import it.innove.play.pdf.PdfGenerator
import javax.inject._
import play.api.Configuration
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._
import services.logic._
import services.logic.killteam.{KTFactionDto, KTFactionLogic, KTTroopLogic, KTTroopNameDto}

import scala.concurrent.duration._

/**
  * Controller which handles all the endpoints for the killteam roster editor
  */
@Singleton
class KTRosterController @Inject()(cc: ControllerComponents, cache: SyncCacheApi, pdfGenerator: PdfGenerator, config: Configuration) extends AbstractController(cc) with I18nSupport {


  implicit val ktFactionDtoWriter = Json.writes[KTFactionDto]
  implicit val ktTroopNameDtoWriter = Json.writes[KTTroopNameDto]


  /**
    * The name of the army cache id in the session
    */
  val SESSION_ARMY_CACHE_ID_NAME = "kt_army_cache_id"


  val cachetimeOut: Int = config.getOptional[Int]("deadzone.cacheTimeOut").getOrElse(15)

  /**
    * Returns all the available factions as a json array
    *
    * @return
    */
  @JSRoute def getFactions = Action {
    Ok(Json.toJson(KTFactionLogic.getAllFactions()))
  }

  @JSRoute def getPopOverData(popoverType: String, key: String) = Action {
    implicit request =>
      Ok(views.html.displayDescription(popoverType, key))
  }

  /**
    * Returns all troops of the given faction for selection
    *
    * @param factionName the name of the faction
    * @return
    */
  @JSRoute def getSelectTroopsForFaction(factionName: String) = Action {

    val troopsForFaction = KTTroopLogic.getAllTroppsForFaction(faction = factionName)
    Ok(Json.toJson(troopsForFaction))
  }



  /**
    * Renews the army in the cache and returns it.
    *
    * @param request
    * @return
    */
  private def renewArmyInCache(request: Request[Any]): ArmyDto = {
    val armyFromCache = getArmyFromCache(request)
    writeArmyToCache(request, armyFromCache)
    armyFromCache
  }

  /**
    * Returns a response with the cacheid in the seeion
    *
    * @param result
    * @param request
    * @return
    */
  private def withCacheId(result: Result, request: Request[Any]): Result = {
    val cacheId = getCacheIdFromSession(request)
    result.withSession(request.session + (SESSION_ARMY_CACHE_ID_NAME -> cacheId))
  }

  /**
    * Reads the army from the cache
    *
    * @param request
    * @return
    */
  private def getArmyFromCache(request: Request[Any]) = {
    val cacheId = getCacheIdFromSession(request)
    cache.get[ArmyDto](cacheId).getOrElse(ArmyDto(""))
  }

  /**
    * Writes the army to the cache
    *
    * @param request
    * @param army
    */
  private def writeArmyToCache(request: Request[Any], army: ArmyDto): Unit = {
    val cacheId = getCacheIdFromSession(request)
    cache.set(cacheId, army, cachetimeOut.minutes)
  }

  /**
    * Gets the cache id from the session
    *
    * @param request
    * @return
    */
  private def getCacheIdFromSession(request: Request[Any]): String = {
    request.session.get(SESSION_ARMY_CACHE_ID_NAME).getOrElse(UUID.randomUUID.toString)
  }

}
