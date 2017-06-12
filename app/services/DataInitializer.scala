package services

import javax.inject.{Inject, Singleton}

import deadzone.parsers.{CSVFactionsImporter, CSVWeaponImporter}
import models._
import play.api.{Configuration, Logger}

/**
  * When configured it will initialize the main data set from some csvs
  *
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 21:37
  */
@Singleton class DataInitializer @Inject()(configuration: Configuration) {

  val reinitData = configuration.getBoolean("deadzone.reinitData")

  if (reinitData.isEmpty == false && reinitData.get == true) {
    cleanDatabase()
  }

  if(FactionDAO.countAll() == 0) {
    Logger.info("No Factions found in db filling up from the csv data")
    importFactions()
  }


  private def importFactions() : Unit = {
    val factions = CSVFactionsImporter.getAllAvaibleFactions
    factions.foreach(factionName => {
      val factionDo = FactionDAO.addFaction(factionName)

      val weaponsDto = CSVWeaponImporter.getWeaponsForFaction(factionName)
      weaponsDto.foreach(weaponDto => {
           WeaponDAO.addWeaponToFaction(weaponDto,factionDo)
      })

      val soldierDtos = CSVFactionsImporter.getSoldierForFaction(factionName)
      soldierDtos.foreach(soldierDto => {
        TroopDAO.addFromSoldierDto(soldierDto,factionDo)
      })
    })
  }

  /**
    * Clean the database from existing data files
    */
  private def cleanDatabase(): Unit = {
    Logger.info("Deleting data from database.")
    DefaultTroopAbilityDAO.deleteAll()
    DefaultWeaponAbilityDAO.deleteAll()
    AbilityDAO.deleteAll()
    TroopDAO.deleteAll()
    WeaponDAO.deleteAll()
    WeaponTypeDAO.deleteAll()
    FactionDAO.deleteAll()
  }
}
