package controllers

import javax.inject._
import killteam.logic.{KTAbilityLogic, KTFactionLogic, KTItemLogic, KTWeaponLogic}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.i18n.Langs


/**
  * The home controller for the Killteam Roster
  */
@Singleton
class KTHomeController @Inject()(cc: ControllerComponents, langs: Langs, mainTpl: views.html.killteamviews.main) extends AbstractController(cc) with I18nSupport {

  implicit val main = mainTpl

  def displayFirstFactionOverview() = Action {

    val firstFactionName = KTFactionLogic.getAllFactions().head.name
    Redirect(routes.KTHomeController.displayFactionOverview(firstFactionName))
  }

  /**
    * Display overview of a fraction
    *
    * @return
    */
  def displayFactionOverview(factionName: String) = Action {
    implicit request =>
      val allFactions = KTFactionLogic.getAllFactions()

      val factionInfos = KTFactionLogic.getFactionInfos(factionName = factionName)

      Ok(views.html.killteamviews.displayFactionOverview(factionInfos, factionName, allFactions))
  }


}
