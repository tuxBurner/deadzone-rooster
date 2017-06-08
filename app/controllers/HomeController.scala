package controllers

import javax.inject._

import models.{FactionDAO, TroopDAO}
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {


  def factionSelect = Action {
    val factions = FactionDAO.getAll()
    Ok(views.html.factionSelect(factions))
  }

  def displayFactionTroops(factionName:String) = Action {
    val troops = TroopDAO.findAllForFactionByName(factionName)
    Ok(views.html.factionTroops(troops))
  }



}
