package services.logic

import models.TroopDAO

import scala.collection.JavaConversions._

object TroopLogic {

  def getSelectTroopsForFaction(factionName: String) : Map[String,List[TroopSelectDto]] = {
    val troops = TroopDAO.findAllForFactionByName(factionName).map(troopDo => {
      // calculate the points from the weapon to the troop
      var points = troopDo.points
      troopDo.defaultWeapons.toList.foreach(defWeapon => {
        points+=defWeapon.points
      })

      TroopSelectDto(troopDo.name, troopDo.modelType, points, troopDo.victoryPoints, troopDo.imageUrl)
    })

    troops.groupBy(_.modelType)
  }
}

case class TroopSelectDto(name:String, modelType: String, points: Int, victoryPoints:Int, imageUrl: String)
