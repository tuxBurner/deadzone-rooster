package killteam

import better.files
import deadzone.parsers.CSVDataParser
import io.methvin.better.files.RecursiveFileMonitor
import javax.inject.{Inject, Singleton}
import killteam.models._
import killteam.parsers._
import play.Logger
import play.api.Configuration

/**
  * When configured it will initialize the main data set from some csvs
  *
  * @author Sebastian Hardt (s.hardt@micromata.de)
  */
@Singleton
class KTDataInitializer @Inject()(configuration: Configuration,
                                  armyParser: KTCSVArmyParser,
                                  weaponParser: KTCSVWeaponParser,
                                  itemParser: KTCSVItemParser,
                                  loadoutParser: KTCSVLoadoutParser,
                                  specialistsParser: KTCSVSpecialistsParser) {


  startImportingData()


  /**
    * Starts importing the data.
    * When an external config folder is found a file watcher is registered on this folder.
    * This keeps track when the user changes a file and reloads the data.
    */
  private def startImportingData(): Unit = {

    importData()

    val externalCfgFolder = CSVDataParser.checkAndGetExternalConfigFolder(configuration)

    // when an external cfg folder is found start a watcher for reloading.
    if (externalCfgFolder.isDefined) {
      Logger.info("Found external config folder starting file change watching")
      val watcher = new RecursiveFileMonitor(better.files.File(externalCfgFolder.get.getAbsolutePath)) {


        override def onModify(file: files.File, count: Int): Unit = {
          if (file.name.endsWith(".csv")) {
            logAndReload(s"$file got changed reloading factions")
          }
        }
      }
      import scala.concurrent.ExecutionContext.Implicits.global
      watcher.start()
    }

    /**
      * Is called when a file changed
      *
      * @param msg the message to print before reloading the factions data
      */
    def logAndReload(msg: String): Unit = synchronized {
      Logger.info(msg)
      importData()
    }
  }


  /**
    * Imports all factions and its troops, weapons, abilities and items.
    */
  def importData(): Unit = {

    Logger.info("#######################################")
    Logger.info("### Start Parsing the Killteam data ###")
    Logger.info("#######################################")

    // clear the database
    KTFactionDao.deleteAll()

    // read all data from the csvs
    armyParser.refresh()
    weaponParser.refresh()
    itemParser.refresh()
    loadoutParser.refresh()
    specialistsParser.refresh()

    // populate specialists
    KTSpecialistDao.addSpecialists(specialistsParser.specialists)

    // repopulate the data
    armyParser.getFactions.foreach(factionName => {
      // add the faction to the database
      val factionDo = KTFactionDao.findOrAddFaction(factionName)

      // gets all weapons for the faction from the csv
      weaponParser.getWeaponsForFaction(factionName).foreach(weaponDto => {
        KTWeaponDao.addWeaponToFaction(weaponDto, factionDo)
      })

      // adds all items to the faction
      itemParser.getItemsForFaction(factionName).foreach(itemDto => {
        KTItemsDao.addItemToFaction(itemDto, factionDo)
      })

      // adds all troops to the faction
      armyParser.getTroopsForFaction(factionName).foreach(troopDto => {
        val troopDo = KTTroopDao.addTroopToFaction(troopDto, factionDo)

        // add the loadouts for the troop
        loadoutParser.getLoadoutsForTroopAndFaction(troopName = troopDo.name, factionName = factionDo.name)
          .foreach(loadOutDto => {
            KTLoadoutDao.addLoadoutToFactionAndTroop(csvLoadoutDto = loadOutDto, factionDo = factionDo, troopDo = troopDo)
          })
      })


    })


    Logger.info("--------------------------------------")
    Logger.info("--- Done Parsing the Killteam data ---")
    Logger.info("--------------------------------------")

  }

}
