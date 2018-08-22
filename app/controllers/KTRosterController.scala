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
import killteam.logic._

import scala.concurrent.duration._

/**
  * Controller which handles all the endpoints for the killteam roster editor
  */
@Singleton
class KTRosterController @Inject()(cc: ControllerComponents, cache: SyncCacheApi, pdfGenerator: PdfGenerator, config: Configuration, mainTpl: views.html.killteamviews.main) extends AbstractController(cc) with I18nSupport {


  implicit val ktFactionDtoWriter = Json.writes[KTFactionDto]
  implicit val ktTroopNameDtoWriter = Json.writes[KTTroopSelectDto]


  /**
    * The name of the army cache id in the session
    */
  val KT_SESSION_ARMY_CACHE_ID_NAME = "kt_army_cache_id"


  val cachetimeOut: Int = config.getOptional[Int]("killteam.cacheTimeOut").getOrElse(15)

  implicit val mainTplImpl = mainTpl


  /**
    * Display the main view
    *
    * @return
    */
  def rosterMain = Action {
    implicit request =>
      withCacheId(Ok(views.html.killteamviews.roster.roster(getArmyFromCache(request))), request)
  }

  /**
    * Returns all the available factions as a json array
    *
    * @return
    */
  @JSRoute
  def getFactions = Action {
    implicit request =>
      Ok(Json.toJson(KTFactionLogic.getAllOrTheOneFromTheArmyFactions(getArmyFromCache(request))))
  }


  /**
    * Returns all troops of the given faction for selection
    *
    * @param factionName the name of the faction
    * @return
    */
  @JSRoute
  def getSelectTroopsForFaction(factionName: String) = Action {
    val troopsForFaction = KTTroopLogic.getAllSelectTroopsForFaction(faction = factionName)
    Ok(Json.toJson(troopsForFaction))
  }

  /**
    * Adds a troop to the army
    *
    * @return
    */
  @JSRoute
  def addTroopToArmy() = Action(parse.tolerantJson) {
    implicit request =>

    val factionName = (request.body \ "faction").as[String]
    val troopName = (request.body \ "troop").as[String]

    val armyFromCache = getArmyFromCache(request)
    val armyWithNewTroop = KTArmyLogic.addTroopToArmy(factionName, troopName, armyFromCache)

    writeArmyToCache(request, armyWithNewTroop)
    withCacheId(Ok(""), request)
  }

  /**
    * Removes the given troop from the army
    * @param uuid
    * @return
    */
  def removeTroopFromArmy(uuid: String) = Action {
    implicit request =>

      val armyFromCache = getArmyFromCache(request)
      val armyWithNewTroop = KTArmyLogic.removeTroopFromArmy(uuid, armyFromCache)
      writeArmyToCache(request, armyWithNewTroop)

    Redirect(routes.KTRosterController.rosterMain())
  }


  /**
    * Renews the army in the cache and returns it.
    *
    * @param request
    * @return
    */
  private def renewArmyInCache(request: Request[Any]): KTArmyDto = {
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
    result.withSession(request.session + (KT_SESSION_ARMY_CACHE_ID_NAME -> cacheId))
  }

  /**
    * Reads the army from the cache
    *
    * @param request
    * @return
    */
  private def getArmyFromCache(request: Request[Any]) = {
    val cacheId = getCacheIdFromSession(request)
    cache.get[KTArmyDto](cacheId).getOrElse(KTArmyDto(""))
  }

  /**
    * Writes the army to the cache
    *
    * @param request
    * @param army
    */
  private def writeArmyToCache(request: Request[Any], army: KTArmyDto): Unit = {
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
    request.session.get(KT_SESSION_ARMY_CACHE_ID_NAME).getOrElse(UUID.randomUUID.toString)
  }

}
