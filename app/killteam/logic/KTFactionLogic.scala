package killteam.logic

import killteam.models.KTFactionDao

object KTFactionLogic {

  /**
    * Gets all faction known for killteam
    * @return
    */
  def getAllOrTheOneFromTheArmyFactions(armyDto: KTArmyDto) : List[KTFactionDto] = {
    if(armyDto.faction.isEmpty) {
     getAllFactions()
    } else {
      List(KTFactionDto(name = armyDto.faction))
    }
  }

  /**
    * Gets all factions
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
