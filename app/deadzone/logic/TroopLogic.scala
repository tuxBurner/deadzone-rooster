package deadzone.logic

import deadzone.models.TroopDAO


/**
  * Operations on the troops
  */
object TroopLogic {

  /**
    * Gets the troops for the select drop down for the given faction
    *
    * @param factionName the name of the faction
    * @return
    */
  def getSelectTroopsForFaction(factionName: String): Map[String, List[TroopSelectDto]] = {
    val troops = TroopDAO.findAllForFactionByName(factionName).map(troopDo => {
      // calculate the points from the weapon to the troop
      var points = troopDo.soldierDto.points
      troopDo.defaultWeapons.foreach(defWeapon => {
        points += defWeapon.points
      })

      TroopSelectDto(name = troopDo.soldierDto.name,
        modelType = troopDo.soldierDto.soldierType.toString,
        points = points,
        victoryPoints = troopDo.soldierDto.victoryPoints,
        imageUrl = troopDo.soldierDto.imageUrl)
    })

    troops.groupBy(_.modelType)
  }

  /**
    * Gets all troops for a faction as an [[ArmyTroopDto]]
    *
    * @param factionName the name of the faction which we want to get the troops for
    * @return a map where  the key is the type of the troop and the value is the list of troops
    */
  def getAllTroopsForFaction(factionName: String): Map[String, List[TroopWithAllowedWeaponsDto]] = {
    TroopDAO.findAllForFactionByName(factionName).map(troopDo => {
      val troopDto = ArmyLogic.troopDoToArmyTroopDto(troopDo)
      val allowedWeapons = ArmyLogic.getWeaponsForTroop(troopDto)
      TroopWithAllowedWeaponsDto(troopDto, allowedWeapons)

    }).groupBy(_.troopDto.modelType)
  }
}

/**
  * Class which holds the troop itself and all the allowed weapon for it
  * @param troopDto the troop
  * @param allowedWeaponsDto all weapons sorted by type
  */
case class TroopWithAllowedWeaponsDto(troopDto: ArmyTroopDto,
                                      allowedWeaponsDto: Map[String, List[ArmyWeaponDto]])


/**
  * Class for displaying a troop in the select box
  *
  * @param name
  * @param modelType
  * @param points
  * @param victoryPoints
  * @param imageUrl
  */
case class TroopSelectDto(name: String,
                          modelType: String,
                          points: Int,
                          victoryPoints: Int,
                          imageUrl: String)
