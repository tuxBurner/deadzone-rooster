package killteam.logic

import killteam.models.KTFactionDao

object KTFactionLogic {

  /**
    * Gets all faction known for killteam
    *
    * @return
    */
  def getAllOrTheOneFromTheArmyFactions(armyDto: KTArmyDto): List[KTFactionDto] = {
    if (armyDto.faction.isEmpty) {
      getAllFactions()
    } else {
      List(KTFactionDto(name = armyDto.faction))
    }
  }

  /**
    * Gets all factions
    *
    * @return
    */
  def getAllFactions(): List[KTFactionDto] = {
    KTFactionDao.factions
      .sortBy(_.name)
      .map(factionDo => KTFactionDto(name = factionDo.name))
      .toList
  }


  /**
    * Gathers all informations to the given faction
    * @param factionName the name of the faction to gather the information's for
    * @return
    */
  def getFactionInfos(factionName: String): KTFactionInfosDto = {
    val allFactionWeapons = KTWeaponLogic.getAllWeaponsByFactionName(factionName = factionName)
    val allFactionItems = KTItemLogic.getAllItemsForFaction(factionName = factionName)
    val allFactionAbilities = KTAbilityLogic.getAllAbilitiesForFaction(factionName = factionName)
    val allTactics = KTTacticsLogic.getTacticsForFaction(factionName = factionName)

    KTFactionInfosDto(allWeapons = allFactionWeapons,
      allItems = allFactionItems,
      allAbilities = allFactionAbilities,
      allTactics = allTactics)
  }


}

/**
  * Represents a killteam faction
  *
  * @param name
  */
case class KTFactionDto(name: String)

/**
  * Contains all informations for a faction
  *
  * @param allWeapons   all weapons for the faction
  * @param allItems     all items for the faction
  * @param allAbilities all abilities for the faction
  * @param allTactics   all tactics for the faction
  */
case class KTFactionInfosDto(allWeapons: List[KTWeaponDto],
                             allItems: List[KTItemDto],
                             allAbilities: List[KTAbilityDto],
                             allTactics: List[KTTacticDto])
