package controllers

import java.util.UUID
import javax.inject._

import com.github.tuxBurner.jsAnnotations.JSRoute
import play.api.cache.CacheApi
import play.api.libs.json._
import play.api.mvc._
import services.logic._

import scala.concurrent.duration._

/**
  * Controller which handles all the endpoints for the rooster editor
  */
@Singleton class RoosterController @Inject()(cache: CacheApi) extends Controller {

  implicit val armyAbilityDtoFormat = Json.format[ArmyAbilityDto]
  implicit val armyWeaponDtoFormat = Json.format[ArmyWeaponDto]
  implicit val armyTroopDtoFormat = Json.format[ArmyTroopDto]
  implicit val armyDtoFormat = Json.format[ArmyDto]
  implicit val factionDtoFormat = Json.format[FactionDto]

  /**
    * Returns all the avaible factions as a json array
    *
    * @return
    */
  @JSRoute def getFactions = Action {

    Ok(Json.toJson(FactionLogic.getAllFactions()))
  }

  /**
    * Returns all troops of the given faction for selection
    *
    * @param factionName the name of the faction
    * @return
    */
  @JSRoute def getSelectTroopsForFaction(factionName: String) = Action {
    implicit val implicitDtoFromat = Json.format[TroopSelectDto]
    Ok(Json.toJson(TroopLogic.getSelectTroopsForFaction(factionName)))
  }

  @JSRoute def addTroopToSelection() = Action(parse.tolerantJson) { request =>

    val factionName = (request.body \ "faction").as[String]
    val troopName =  (request.body \ "troop").as[String]

    val armyFromCache = getArmyFromCache(request,factionName)
    val armyWithNewTroop = ArmyLogic.addTroopToArmy(factionName,troopName,armyFromCache)

    writeArmyToCache(request,armyWithNewTroop)
    withCacheId(Ok(Json.toJson(armyWithNewTroop)).as(JSON),request)
  }

  @JSRoute def getArmy() = Action { request =>
    val factionName = request.getQueryString("faction").get
    val army = getArmyFromCache(request,factionName)
    writeArmyToCache(request,army)
    withCacheId(Ok(Json.toJson(army)).as(JSON),request)
  }

  private def withCacheId(result:Result, request: Request[Any]):Result =  {
    val cacheId = getCacheIdFromSession(request)
    result.withSession(request.session +  ("test" -> cacheId))
  }

  private def getArmyFromCache(request:Request[Any],factionName: String) = {
    val cacheId = getCacheIdFromSession(request)
    cache.getOrElse[ArmyDto](cacheId) {
      ArmyDto("",factionName)
    }
  }

  private def writeArmyToCache(request:Request[Any],army:ArmyDto): Unit = {
    val cacheId = getCacheIdFromSession(request)
    cache.set(cacheId, army, 5.minutes)
  }

  private def getCacheIdFromSession(request:Request[Any]): String = {
    request.session.get("test").getOrElse(UUID.randomUUID.toString)
  }

}
