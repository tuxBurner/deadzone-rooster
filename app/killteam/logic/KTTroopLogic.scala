package killteam.logic

import killteam.models.KTTroopDao

object KTTroopLogic {


  /**
    * Gets all select troops dto for the given faction
    *
    * @param faction the faction to get the troops for
    * @return
    */
  def getAllSelectTroopsForFaction(faction: String, armyDto: KTArmyDto): List[KTTroopSelectDto] = {
    KTTroopDao.getAllTroopsOfFaction(factionName = faction)
      .toList
      .filter(troopDo => troopDo.requiredUnits.isEmpty || armyDto.troops.exists(armyTroop => troopDo.requiredUnits.contains(armyTroop.unit))) // filter if there are troops which are required
      .filter(troopDo => troopDo.maxInArmy == 0 || armyDto.troops.filter(armyTroop => armyTroop.name == troopDo.name).map(_.amount).sum < troopDo.maxInArmy) // filter where there are more troops than max allowed
      .sortBy(_.name)
      .map(troopDo => KTTroopSelectDto(name = troopDo.name, points = troopDo.points))
  }


  /**
    * Counts how often the troop is found in the army
    *
    * @param troopDto the troop to count
    * @param armyDto  the army where the troop is in
    * @return
    */
  def countAmountInArmy(troopDto: KTArmyTroopDto, armyDto: KTArmyDto): Int = {
    armyDto.troops
      .filter(troopInArmy => troopInArmy.name == troopDto.name)
      .map(_.amount)
      .sum
  }

}

/**
  * Represents a troop for the drop down select in the frontend
  *
  * @param name   the name of the troop
  * @param points the troop points
  */
case class KTTroopSelectDto(name: String,
                            points: Int)
