package controllers

import javax.inject._
import killteam.logic.KTGeneralArmyDataLogic
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.i18n.Langs


/**
  * The home controller for the Killteam Roster
  */
@Singleton
class KTHomeController @Inject()(cc: ControllerComponents, langs: Langs, mainTpl: views.html.killteamviews.main) extends AbstractController(cc) with I18nSupport {

  implicit val main = mainTpl

  /**
    * Display all weapons grouped by the faction they belong to
    * @return
    */
  def displayAllWeapons = Action {
    implicit request =>
      val weaponsGroupedByFaction = KTGeneralArmyDataLogic.getAllWeaponsGroupedByFaction()
      Ok(views.html.killteamviews.displayAllWeapons(weaponsGroupedByFaction))
  }


}
