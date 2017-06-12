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

  /**
    * Returns all the avaible factions as a json array
    *
    * @return
    */
  @JSRoute def getFactions = Action {
    implicit val implicitDtoFromat = Json.format[FactionDto]
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

    implicit val armyAbilityDtoFormat = Json.format[ArmyAbilityDto]
    implicit val armyTroopDtoFormat = Json.format[ArmyTroopDto]
    implicit val armyDtoFormat = Json.format[ArmyDto]

    val cacheId:String = request.session.get("test").map(value => value).getOrElse(UUID.randomUUID.toString)

    val armyFromCache = cache.getOrElse[ArmyDto](cacheId) {
      ArmyDto("",factionName)
    }

    val armyWithNewTroop = ArmyLogic.addTroopToArmy(factionName,troopName,armyFromCache)

    cache.set(cacheId, armyWithNewTroop, 5.minutes)
    

    Ok(Json.toJson(armyWithNewTroop)).as(JSON).withSession(request.session +  ("test" -> cacheId))
  }

}
