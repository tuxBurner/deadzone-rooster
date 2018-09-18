package controllers

import java.util.UUID

import com.github.tuxBurner.jsAnnotations.JSRoute
import it.innove.play.pdf.PdfGenerator
import javax.inject._
import killteam.logic._
import play.api.Configuration
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._
import views.html.killteamviews.main

import scala.concurrent.duration._

/**
  * Controller which handles all the endpoints for the killteam roster editor
  */
@Singleton
class KTRosterController @Inject()(cc: ControllerComponents, cache: SyncCacheApi, pdfGenerator: PdfGenerator, config: Configuration, mainTpl: views.html.killteamviews.main) extends AbstractController(cc) with I18nSupport {


  implicit val ktFactionDtoWriter: OWrites[KTFactionDto] = Json.writes[KTFactionDto]
  implicit val ktTroopNameDtoWriter: OWrites[KTTroopSelectDto] = Json.writes[KTTroopSelectDto]
  implicit val mainTplImpl: main = mainTpl


  /**
    * The name of the army cache id in the session
    */
  val KT_SESSION_ARMY_CACHE_ID_NAME = "kt_army_cache_id"


  val cachetimeOut: Int = config.getOptional[Int]("killteam.cacheTimeOut").getOrElse(15)


  /**
    * Display the main view
    *
    * @return
    */
  def rosterMain = Action {
    implicit request =>
      getArmyFromCacheAndHandleResult(army => {
        Ok(views.html.killteamviews.roster.roster(army))
      })
  }


  /**
    * Displays the edit options for the given troop
    *
    * @param uuid the troop to edit
    * @return
    */
  def displayEditOptions(uuid: String) = Action {
    implicit request =>
      getArmyFromCacheAndHandleResult(army => {
        KTArmyLogic.getPossibleConfigurationOptionsForTroop(uuid, army)
          .map(troopConfigOption => {
            Ok(views.html.killteamviews.roster.editTroop(troopConfigOption))
          })
          .getOrElse({
            Redirect(routes.KTRosterController.rosterMain())
          })
      })
  }

  /**
    * Returns all the available factions as a json array
    *
    * @return
    */
  @JSRoute
  def getFactions = Action {
    implicit request =>
      getArmyFromCacheAndHandleResult(army => {
        Ok(Json.toJson(KTFactionLogic.getAllOrTheOneFromTheArmyFactions(army)))
      })
  }


  /**
    * Returns all troops of the given faction for selection
    *
    * @param factionName the name of the faction
    * @return
    */
  @JSRoute
  def getSelectTroopsForFaction(factionName: String) = Action {
    implicit request =>
      getArmyFromCacheAndHandleResult(army => {
        val troopsForFaction = KTTroopLogic.getAllSelectTroopsForFaction(faction = factionName, armyDto = army)
        Ok(Json.toJson(troopsForFaction))
      })
  }

  /**
    * Gets a list of specialists this troop can have
    *
    * @param troopName   the name of the tropp
    * @param factionName the name of the faction the troop belongs to
    * @return
    */
  @JSRoute
  def getSelectSpecialistsForTroop(troopName: String, factionName: String) = Action {
    implicit request =>
      getArmyFromCacheAndHandleResult(army => {
        val specialists = KTSpecialistLogic.getAvaibleSpecialistSelectOptions(troopName = troopName, factionName = factionName, army)
        Ok(Json.toJson(specialists))
      })
  }

  /**
    * Adds a troop to the army
    *
    * @return
    */
  @JSRoute
  def addTroopToArmy() = Action(parse.tolerantJson) {
    implicit request =>

      val factionName = (request.body \ "faction").as[String]
      val troopName = (request.body \ "troop").as[String]
      val specialistName = (request.body \ "specialist").as[String]

      getArmyFromCacheAndUpdateIt(army => {
        KTArmyLogic.addTroopToArmy(factionName = factionName,
          troopName = troopName,
          specialistName = specialistName,
          armyDto = army)
      }, Ok(""))

  }

  /**
    * Removes the given troop from the army
    *
    * @param uuid the uuid of the troop to remove
    * @return
    */
  def removeTroopFromArmy(uuid: String) = Action {
    implicit request =>
      getArmyFromCacheAndUpdateIt(armyFromCache => KTArmyLogic.removeTroopFromArmy(uuid = uuid, armyDto = armyFromCache),
        Redirect(routes.KTRosterController.rosterMain()))
  }

  /**
    * Sets a new loadout at the given troop
    *
    * @param uuid        the uuid of the troop
    * @param loadoutName the name of the loadout to set
    * @return
    */
  def setNewLoadoutAtTroop(uuid: String, loadoutName: String) = Action {
    implicit request =>
      getArmyFromCacheAndUpdateIt(armyFromCache => KTArmyLogic.setLoadoutAtTroop(loadoutName = loadoutName, uuid = uuid, armyDto = armyFromCache),
        Redirect(routes.KTRosterController.displayEditOptions(uuid)))
  }


  /**
    * Adds a new item to the troop
    *
    * @param uuid     the uuid of the troop
    * @param itemName the name of the item to add
    * @return
    */
  def addItemToTroop(uuid: String, itemName: String) = Action {
    implicit request =>
      getArmyFromCacheAndUpdateIt(armyFromCache => KTItemLogic.setItemAtTroop(itemName = itemName, uuid = uuid, armyDto = armyFromCache),
        Redirect(routes.KTRosterController.displayEditOptions(uuid)))
  }

  /**
    * Adds a new item to the troop
    *
    * @param uuid     the uuid of the troop
    * @param itemName the name of the item to remove
    * @return
    */
  def removeItemFromTroop(uuid: String, itemName: String) = Action {
    implicit request =>
      getArmyFromCacheAndUpdateIt(armyFromCache => KTItemLogic.removeItemFromTroop(itemName = itemName, uuid = uuid, armyDto = armyFromCache),
        Redirect(routes.KTRosterController.displayEditOptions(uuid)))
  }


  /**
    * Adds a special to the given troop at the given level
    *
    * @param uuid         the uuid of the troop
    * @param specialName  the name of the special to add
    * @param specialLevel the level of the special to set
    * @return
    */
  def addSpecialToTroop(uuid: String, specialName: String, specialLevel: Int) = Action {
    implicit request =>
      getArmyFromCacheAndUpdateIt(armyFromCache => KTSpecialistLogic.setSpecialAtTroop(specialName = specialName, specialLevel = specialLevel, uuid = uuid, armyDto = armyFromCache),
        Redirect(routes.KTRosterController.displayEditOptions(uuid)))
  }

  /**
    * Sets the given level at the troop
    *
    * @param uuid  the uuid of the troop to set the given level at
    * @param level the level to set
    * @return
    */
  def setLevelAtTroop(uuid: String, level: Int) = Action {
    implicit request =>
      getArmyFromCacheAndUpdateIt(armyFromCache => KTSpecialistLogic.setLevelAtTroop(uuid = uuid, level = level, armyDto = armyFromCache),
        Redirect(routes.KTRosterController.displayEditOptions(uuid)))
  }

  /**
    * Sets the amount of the troop in the army
    * @param uuid the uuid of the troop to set the amount of
    * @param amount the amount of troops in the army with this configuration
    * @return
    */
  def setAmountOfTroopInArmy(uuid: String, amount: Int) = Action {
    implicit request =>
      getArmyFromCacheAndUpdateIt(armyFromCache => KTArmyLogic.setAmountOfTroopInArmy(uuid = uuid, amount = amount, armyDto = armyFromCache),
        Redirect(routes.KTRosterController.rosterMain()))
  }

  /**
    * Gets the table pdf
    *
    * @return
    */
  def getTablePdf() = Action {
    implicit request =>
      getArmyFromCacheAndHandleResult(army => {
        val armyAbilities = KTAbilityLogic.getAllAbilitiesFromArmy(army)
        val pdfBytes = pdfGenerator.toBytes(views.html.killteamviews.pdf.rosterTable.render(army, armyAbilities, messagesApi.preferred(request)), "http://localhost:9000")
        Ok(pdfBytes).as("application/pdf").withHeaders("Content-Disposition" -> "inline; filename=rooster.pdf")
      })
  }

  /**
    * Returns a response with the cacheid in the seeion
    *
    * @param result
    * @param request
    * @return
    */
  private def withCacheId(result: Result)(implicit request: Request[Any]): Result = {
    val cacheId = getCacheIdFromSession(request)
    result.withSession(request.session + (KT_SESSION_ARMY_CACHE_ID_NAME -> cacheId))
  }

  /**
    * Reads the army from the cache
    *
    * @param request
    * @return
    */
  private def getArmyFromCache()(implicit request: Request[Any]): KTArmyDto = {
    val cacheId = getCacheIdFromSession(request)
    cache.get[KTArmyDto](cacheId).getOrElse(KTArmyDto())
  }

  /**
    * Writes the army to the cache
    *
    * @param request
    * @param army
    */
  private def writeArmyToCache(request: Request[Any], army: KTArmyDto): Unit = {
    val cacheId = getCacheIdFromSession(request)
    cache.set(cacheId, army, cachetimeOut.minutes)
  }

  /**
    * Gets the cache id from the session
    *
    * @param request
    * @return
    */
  private def getCacheIdFromSession(request: Request[Any]): String = {
    request.session.get(KT_SESSION_ARMY_CACHE_ID_NAME).getOrElse(UUID.randomUUID.toString)
  }

  /**
    * Gets the army from the cache and performs a change operation on it
    *
    * @param changeArmyFunction the function changing the army
    * @param request            the request which holds the session information's
    * @return
    */
  private def getArmyFromCacheAndUpdateIt(changeArmyFunction: (KTArmyDto) => KTArmyDto, result: Result)(implicit request: Request[Any]): Result = {
    getArmyFromCacheAndHandleResult(army => {
      val changedArmy = changeArmyFunction(army)
      writeArmyToCache(request, changedArmy)
      result
    })
  }


  /**
    * Gets the army from cache and handles the result with it
    *
    * @param handleArmyFunction the function doing something with the army and returning a result
    * @param request            the request from the user
    * @return
    */
  private def getArmyFromCacheAndHandleResult(handleArmyFunction: (KTArmyDto) => Result)(implicit request: Request[Any]): Result = {
    val armyFromCache = getArmyFromCache()
    val result = handleArmyFunction(armyFromCache)
    withCacheId(result)
  }

}
