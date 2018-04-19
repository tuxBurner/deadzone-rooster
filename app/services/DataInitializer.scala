package services

import javax.inject.{Inject, Singleton}

import better.files
import deadzone.parsers.{CSVDataParser, CSVArmyImporter, CSVItemsImporter, CSVWeaponImporter}
import io.methvin.better.files.RecursiveFileMonitor
import models._
import play.Logger
import play.api.Configuration

/**
  * When configured it will initialize the main data set from some csvs
  *
  * @author Sebastian Hardt (s.hardt@micromata.de)
  */
@Singleton class DataInitializer @Inject()(configuration: Configuration,
                                           csvFactionsImporter: CSVArmyImporter,
                                           csvWeaponImporter: CSVWeaponImporter,
                                           csvItemsImporter: CSVItemsImporter) {


  startImportingData()


  /**
    * Starts importing the data.
    * When an external config folder is found a file watcher is registered on this folder.
    * This keeps track when the user changes a file and reloads the data.
    */
  private def startImportingData(): Unit = {

    importFactions()

    val externalCfgFolder = CSVDataParser.checkAndGetExternalConfigFolder(configuration)

    // when an external cfg folder is found start a watcher for reloading.
    if (externalCfgFolder.isDefined) {
      Logger.info("Found external config folder starting file change watching")
      val watcher = new RecursiveFileMonitor(better.files.File(externalCfgFolder.get.getAbsolutePath)) {



        override def onModify(file: files.File, count: Int): Unit = {
          if(file.name.endsWith(".csv")) {
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
      importFactions()
    }
  }


  /**
    * Imports all factions and its troops, weapons, abilities and items.
    */
  def importFactions(): Unit = {

    FactionDAO.clearAll()
    AbilityDAO.clearAll()
    WeaponDAO.clearAll()
    ItemDAO.clearAll()
    TroopDAO.clearAll()

    csvFactionsImporter.refresh()
    csvItemsImporter.refresh()
    csvWeaponImporter.refresh()

    val factions = csvFactionsImporter.getAllAvaibleFactions
    factions.foreach(factionName => {
      val factionDo = FactionDAO.findOrAddFaction(factionName)

      val weaponsDto = csvWeaponImporter.getWeaponsForFaction(factionName)
      weaponsDto.foreach(weaponDto => {
        WeaponDAO.addWeaponToFaction(weaponDto, factionDo)
      })

      val itemsDto = csvItemsImporter.getItemsForFaction(factionName)
      itemsDto.foreach(itemDto => {
        ItemDAO.addItemToFaction(itemDto, factionDo)
      })

      val soldierDtos = csvFactionsImporter.getSoldierForFaction(factionName)
      soldierDtos.foreach(soldierDto => {
        TroopDAO.addFromCSVSoldierDto(soldierDto, factionDo)
      })
    })

    Logger.info("Done parsing and importing csv data files")
  }

}
