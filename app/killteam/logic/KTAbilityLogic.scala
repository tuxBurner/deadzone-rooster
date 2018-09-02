package killteam.logic

import killteam.models.{KTAbilityDao, KTAbilityDo}

/**
  * Killteam ability logic
  */
object KTAbilityLogic {

  /**
    * Gets all abilities used by the troops in the army
    * @param armyDto the army with the troops with the abilities
    * @return
    */
  def getAllAbilitiesFromArmy(armyDto: KTArmyDto) : List[KTAbilityDto] = {
    armyDto
      .troops.flatMap(troopDto => troopDto.abilities)
      .groupBy(_.name)
      .map(_._2.head)
      .toList
      .sortBy(_.name)
  }


  /**
    * Gets all the abilities of the given faction
    * @param factionName the name of the faction
    * @return
    */
  def getAllAbilitiesForFaction(factionName: String): List[KTAbilityDto] = {
    KTAbilityDao.getAllAbilitiesByFaction(factionName).map(abilityDoToDto)
  }

  /**
    * Converts an ability do to a dto
    *
    * @param abilityDo the do to convert
    * @return
    */
  def abilityDoToDto(abilityDo: KTAbilityDo): KTAbilityDto = {
    KTAbilityDto(name = abilityDo.name,
      faction = abilityDo.faction.name)
  }

}

/**
  * An ability a troop can have
  *
  * @param name    the name of the ability
  * @param faction the name of the faction the ability belongs to
  */
case class KTAbilityDto(name: String,
                        faction: String)
