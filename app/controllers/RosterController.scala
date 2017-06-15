package controllers

import java.util.UUID
import javax.inject._

import com.github.tuxBurner.jsAnnotations.JSRoute
import it.innove.play.pdf.PdfGenerator
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc._
import services.logic._

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

/**
  * Controller which handles all the endpoints for the roster editor
  */
@Singleton class RosterController @Inject()(cache: CacheApi, pdfGenerator: PdfGenerator, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  implicit val armyAbilityDtoFormat = Json.format[ArmyAbilityDto]
  implicit val armyWeaponDtoFormat = Json.format[ArmyWeaponDto]
  implicit val armyItemDtoFormat = Json.format[ArmyItemDto]
  implicit val armyTroopDtoFormat = Json.format[ArmyTroopDto]
  implicit val armyDtoFormat = Json.format[ArmyDto]
  implicit val factionDtoFormat = Json.format[FactionDto]
  implicit val armyTroopWeaponsItemsFormat = Json.format[ArmyTroopWeaponsItemsDto]

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

  /**
    * Adds a troop to the army
    * @return
    */
  @JSRoute def addTroopToArmy() = Action(parse.tolerantJson) { request =>

    val factionName = (request.body \ "faction").as[String]
    val troopName = (request.body \ "troop").as[String]

    val armyFromCache = getArmyFromCache(request)
    val armyWithNewTroop = ArmyLogic.addTroopToArmy(factionName, troopName, armyFromCache)

    writeArmyToCache(request, armyWithNewTroop)
    withCacheId(Ok(Json.toJson(armyWithNewTroop)).as(JSON), request)
  }

  /**
    * Removes a troop from the army
    * @param uuid
    * @return
    */
  @JSRoute def removeTroopFromArmy(uuid: String) = Action { request =>
    val armyFromCache = getArmyFromCache(request)
    val newArmy = ArmyLogic.removeTroopFromArmy(uuid, armyFromCache)
    writeArmyToCache(request, newArmy)
    withCacheId(Ok(Json.toJson(newArmy)).as(JSON), request)
  }

  /**
    * Gets the avaible weapons and items for the given troop
    * @param uuid
    * @return
    */
  @JSRoute def getWeaponsAndItemsForTroop(uuid: String) = Action { request =>
    val armyFromCache = getArmyFromCache(request)
    val result = ArmyLogic.getWeaponsAndItemsForTroop(uuid,armyFromCache)

    renewArmyInCache(request)
    withCacheId(Ok(Json.toJson(result)).as(JSON), request)
  }

  /**
    * Update the troop in the army with the selected weapons and items
    * @param uuid
    * @return
    */
  @JSRoute def updateTroopWeaponsAndItems(uuid:String) = Action(parse.tolerantJson) { request =>
    val armyFromCache = getArmyFromCache(request)

    val selectedWeapons = (request.body \ "selectedWeapons").as[List[String]]
    val selectedItems = (request.body \ "selectedItems").as[List[String]]

    val newArmy = ArmyLogic.updateTroop(uuid,armyFromCache,selectedWeapons,selectedItems)

    writeArmyToCache(request,newArmy)

    withCacheId(Ok(Json.toJson(newArmy)).as(JSON), request)
  }

  /**
    * Gets the current army
    * @return
    */
  @JSRoute def getArmy() = Action { request =>
    val army = getArmyFromCache(request)
    writeArmyToCache(request, army)
    withCacheId(Ok(Json.toJson(army)).as(JSON), request)
  }

  /**
    * Gets the table pdf
    * @return
    */
  def getTablePdf() = Action { request =>
    val army = getArmyFromCache(request)

    val armyPdfInfos = ArmyLogic.extractPdfArmyInfos(army)

    val pdfBytes = pdfGenerator.toBytes(views.html.pdf.rosterTable.render(army,armyPdfInfos,messagesApi.preferred(request)),"http://localhost:9000")
    withCacheId(Ok(pdfBytes),request).as("application/pdf").withHeaders("Content-Disposition" -> "inline; filename=rooster.pdf")
  }

  private def renewArmyInCache(request: Request[Any]): Unit = {
    val armyFromCache = getArmyFromCache(request)
    writeArmyToCache(request, armyFromCache)
  }

  private def withCacheId(result: Result, request: Request[Any]): Result = {
    val cacheId = getCacheIdFromSession(request)
    result.withSession(request.session + ("test" -> cacheId))
  }

  private def getArmyFromCache(request: Request[Any]) = {
    val cacheId = getCacheIdFromSession(request)
    cache.getOrElse[ArmyDto](cacheId) {
      ArmyDto("")
    }
  }

  private def writeArmyToCache(request: Request[Any], army: ArmyDto): Unit = {
    val cacheId = getCacheIdFromSession(request)
    cache.set(cacheId, army, 5.minutes)
  }

  private def getCacheIdFromSession(request: Request[Any]): String = {
    request.session.get("test").getOrElse(UUID.randomUUID.toString)
  }

}
