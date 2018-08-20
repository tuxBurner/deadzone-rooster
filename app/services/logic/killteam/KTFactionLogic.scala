package services.logic.killteam

import models.killteam.KTFactionDao

object KTFactionLogic {

  /**
    * Gets all faction known for killteam
    * @return
    */
  def getAllOrTheOneFromTheArmyFactions(armyDto: KTArmyDto) : List[KTFactionDto] = {

    if(armyDto.faction.isEmpty) {
      KTFactionDao.factions
        .sortBy(_.name)
        .map(factionDo => KTFactionDto(name = factionDo.name))
        .toList
    } else {
      List(KTFactionDto(name = armyDto.faction))
    }
  }

}

/**
  * Represents a killteam faction
  * @param name
  */
case class KTFactionDto(name: String)
