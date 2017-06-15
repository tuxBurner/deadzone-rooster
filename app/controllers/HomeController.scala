package controllers

import javax.inject._

import com.github.tuxBurner.jsAnnotations.JsRoutesComponent
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.routing.{JavaScriptReverseRoute, JavaScriptReverseRouter}

import scala.collection.JavaConversions._


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(jsRoutesComponent:JsRoutesComponent,val messagesApi: MessagesApi) extends Controller with I18nSupport {


  /**
    * Display the main view
    * @return
    */
  def rosterMain = Action {
    Ok(views.html.roster())
  }




  /**
    * Register the routes to certain stuff to the javascript routing so we can
    * reach it better from there
    *
    * @return
    */
  def jsRoutes = Action {
    request =>
      val routes:Array[JavaScriptReverseRoute] = jsRoutesComponent.getJsRoutes.toSet.toArray
      val routeScript = JavaScriptReverseRouter.apply("jsRoutes", Some("jQuery.ajax"),request.host,routes:_*)

      Ok(routeScript.body).as("text/javascript")
  }



}
