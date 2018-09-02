package killteam.logic

import killteam.models.KTWeaponDao

object KTGeneralArmyDataLogic {

  def getAbilities(armyDto: KTArmyDto) : List[KTAbilityDto] = {
    armyDto
      .troops.flatMap(troopDto => troopDto.abilities)
      .groupBy(_.name)
      .map(_._2.head)
      .toList
      .sortBy(_.name)
  }


  /**
    * Gets all weapons grouped by the factions
    * @return
    */
  def getAllWeaponsGroupedByFaction() : Map[String, List[KTWeaponDto]] = {
    KTWeaponDao
      .getWeaponsGroupedByFactions()
      .map(entry => (entry._1, entry._2.map(KTArmyLogic.weaponDoToDto)))
  }

}
