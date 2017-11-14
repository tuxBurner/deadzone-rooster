package services.logic

import models.TroopDAO


object TroopLogic {

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
}

case class TroopSelectDto(name: String,
                          modelType: String,
                          points: Int,
                          victoryPoints: Int,
                          imageUrl: String)
