package controllers

import javax.inject._

import models.FactionDAO
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {


  def index = Action {
    val factions = FactionDAO.getAll()
    Ok(views.html.index(factions))
  }

}
