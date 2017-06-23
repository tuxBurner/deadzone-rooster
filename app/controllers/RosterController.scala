  package controllers

import java.util.UUID
import javax.inject._

import com.github.tuxBurner.jsAnnotations.JSRoute
import it.innove.play.pdf.PdfGenerator
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc._
import services.logic.ArmyImExpLogic.{ArmyImpExpDto, TroopImExpDto}
import services.logic._

import scala.concurrent.duration._
import scala.io.Source

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
  implicit val troopExportDtoFormat = Json.format[TroopImExpDto]
  implicit val armyExpImpDtoFormat = Json.format[ArmyImpExpDto]

  /**
    * Returns all the available factions as a json array
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
    * Endpoint for changing the army's name
    * @return
    */
  @JSRoute def changeArmyName() = Action(parse.tolerantJson) { request =>
    val armyName = (request.body \ "armyName").as[String]
    val armyFromCache = getArmyFromCache(request)
    val newArmy = ArmyLogic.changeNameOfArmy(armyName,armyFromCache)

    writeArmyToCache(request,newArmy)
    withCacheId(Ok(Json.toJson(newArmy)).as(JSON), request)
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
    val armyFromCache = renewArmyInCache(request)
    val result = ArmyLogic.getWeaponsAndItemsForTroop(uuid,armyFromCache)
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
    val army = renewArmyInCache(request)
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

  /**
    * Validates the army against the rule set of deadzone
    * @return
    */
  @JSRoute def validateArmy() = Action { request =>
    val army = getArmyFromCache(request)
    val validator = new ArmyValidator(messagesApi.preferred(request))
    val validationResult = validator.validateArmy(army)
    writeArmyToCache(request, army)
    withCacheId(Ok(Json.toJson(validationResult)).as(JSON), request)
  }

  /**
    * Clones the troop by the given uuid
    * @param uuid
    * @return
    */
  @JSRoute def cloneTroop(uuid: String) = Action { request =>
    val army = getArmyFromCache(request)
    val newArmy = ArmyLogic.cloneTroop(uuid,army)
    writeArmyToCache(request,newArmy)
    withCacheId(Ok(Json.toJson(newArmy)).as(JSON), request)
  }

  /**
    * Exports the army as a json file
    * @return
    */
  @JSRoute def exportArmy() = Action { request =>
    val army = renewArmyInCache(request)
    val exportData = ArmyImExpLogic.armyForExport(army)
    val jsonData = Json.prettyPrint(Json.toJson(exportData))
    val fileName = if(army.name.isEmpty) "army" else army.name
    val headerContent =  "attachement; filename="+fileName+".json";
    withCacheId(Ok(jsonData).as(JSON), request).as(JSON).withHeaders("Content-Disposition" -> headerContent)
  }

  /**
    * Import army from json
    * @return
    */
  @JSRoute def importArmy() = Action(parse.multipartFormData) { request =>
    request.body.file("file").map(file => {
      val jsonVal = Source.fromFile(  file.ref.file).getLines().mkString
      val jsValue = Json.parse(jsonVal)
      armyExpImpDtoFormat.reads(jsValue)
        .map(armyToImport => {
          val army = ArmyImExpLogic.importArmy(armyToImport)
          writeArmyToCache(request,army)
          withCacheId(Ok(Json.toJson(army)),request)
        })
        .getOrElse(InternalServerError(""))
    }).getOrElse(InternalServerError(""))
  }

  private def renewArmyInCache(request: Request[Any]): ArmyDto = {
    val armyFromCache = getArmyFromCache(request)
    writeArmyToCache(request, armyFromCache)
    armyFromCache
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
