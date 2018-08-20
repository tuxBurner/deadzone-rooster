package services.logic.killteam

import models.killteam.KTFactionDao

object KTFactionLogic {

  /**
    * Gets all faction known for killteam
    * @return
    */
  def getAllFactions() : List[KTFactionDto] = {
    KTFactionDao.factions
      .sortBy(_.name)
      .map(factionDo => KTFactionDto(name = factionDo.name))
      .toList
  }

}

/**
  * Represents a killteam faction
  * @param name
  */
case class KTFactionDto(name: String)
