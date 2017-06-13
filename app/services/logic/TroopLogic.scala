package services.logic

import models.TroopDAO

import scala.collection.JavaConversions._

object TroopLogic {

  def getSelectTroopsForFaction(factionName: String) : List[TroopSelectDto] = {
    TroopDAO.findAllForFactionByName(factionName).map(troopDo => {
      // calculate the points from the weapon to the troop
      var points = troopDo.points
      troopDo.defaultWeapons.toList.foreach(defWeapon => {
        points+=defWeapon.points
      })

      TroopSelectDto(troopDo.name, troopDo.modelType, points, troopDo.victoryPoints)
    })
  }
}

case class TroopSelectDto(name:String, modelType: String, points: Int, victoryPoints:Int)
