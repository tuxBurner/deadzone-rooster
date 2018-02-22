package controllers

import javax.inject._

import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class FigureManagerController @Inject()(cc: ControllerComponents, cache: SyncCacheApi) extends AbstractController(cc) with I18nSupport {

  def displayFigureManagerPage() = Action {
    implicit request =>
      Ok(views.html.figuremanager.figureManagerMain())
  }

}
