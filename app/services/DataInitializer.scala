package services

import java.io.File
import java.nio.file.{FileSystem, FileSystems, Paths, StandardWatchEventKinds}
import javax.inject.{Inject, Singleton}

import deadzone.parsers.{CSVDataParser, CSVFactionsImporter, CSVItemsImporter, CSVWeaponImporter}
import models._
import play.api.{Configuration, Logger}

/**
  * When configured it will initialize the main data set from some csvs
  *
  * @author Sebastian Hardt (s.hardt@micromata.de)
  */
@Singleton class DataInitializer @Inject()(configuration: Configuration,
                                           csvFactionsImporter: CSVFactionsImporter,
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

   /* val externalConfigFolder = CSVDataParser.checkAndGetExternalConfigFolder(configuration)
    externalConfigFolder.map(folderFile => {

      val watcher = FileSystems.getDefault.newWatchService()
      Paths.get(folderFile.getAbsolutePath)
        .register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
      val key = watcher.take().pollEvents()
      if (!key.isEmpty) {
        importFactions()
      }
    })*/


  }


  /**
    * Imports all factions and its troops, weapons, abilities and items.
    */
  private def importFactions(): Unit = {

    FactionDAO.clearAll()
    AbilityDAO.clearAll()
    WeaponDAO.clearAll()
    ItemDAO.clearAll()
    TroopDAO.clearAll()

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
  }

}
