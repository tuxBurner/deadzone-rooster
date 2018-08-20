package services.logic.killteam

import models.killteam.KTTroopDao

object KTTroopLogic {

  def getAllTroppsForFaction(faction: String): List[KTTroopNameDto] = {
    KTTroopDao.getAllTroopsOfFaction(factionName = faction)
      .toList
      .sortBy(_.name)
      .map(troopDo => KTTroopNameDto(name = troopDo.name))
  }

}

/**
  * Represents a troop by its name
  *
  * @param name the name of the troop
  */
case class KTTroopNameDto(name: String)
