package controllers

import com.github.tuxBurner.jsAnnotations.JsRoutesComponent
import it.innove.play.pdf.PdfGenerator
import javax.inject._
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.i18n.Langs


/**
  *The home controller for the Killteam Roster
  */
@Singleton
class KTHomeController @Inject()(cc: ControllerComponents, jsRoutesComponent: JsRoutesComponent, pdfGenerator: PdfGenerator, langs: Langs, mainTpl: views.html.killteam.main) extends AbstractController(cc) with I18nSupport {




  


}
