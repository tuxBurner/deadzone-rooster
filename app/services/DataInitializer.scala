package services

import javax.inject.{Inject, Singleton}

import deadzone.parsers.{FactionsImporter, WeaponImporter}
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

  importFactions()


  private def importFactions() : Unit = {
    val factions = FactionsImporter.getAllAvaibleFactions
    factions.foreach(factionName => {
      val factionDo = ArmyFactionDAO.addFaction(factionName)

      val weaponsDto = WeaponImporter.getWeaponsForFaction(factionName)
      weaponsDto.foreach(weaponDto => {
           WeaponDAO.addWeaponToFaction(weaponDto,factionDo)
      })

      val soldierDtos = FactionsImporter.getSoldierForFaction(factionName)
      soldierDtos.foreach(soldierDto => {
        ArmyTroopDAO.addFromSoldierDto(soldierDto,factionDo)
      })
    })
  }

  /**
    * Clean the database from existing data files
    */
  private def cleanDatabase(): Unit = {
    Logger.info("Deleting data from database.")
    DefaulTroopAbilityDAO.deleteAll()
    AbilityDAO.deleteAll()
    WeaponDAO.deleteAll()
    ArmyTroopDAO.deleteAll()
    ArmyFactionDAO.deleteAll()
  }
}
