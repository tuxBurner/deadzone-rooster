package controllers

import javax.inject._

import com.github.tuxBurner.jsAnnotations.JSRoute
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import services.logic.{FactionDto, FactionLogic, TroopLogic, TroopSelectDto}

/**
  * Controller which handles all the endpoints for the rooster editor
  */
@Singleton class RoosterController @Inject() extends Controller {

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

    

    Ok("MUUUH")
  }

}
