package services.logic

import models.TroopDAO


object TroopLogic {

  def getSelectTroopsForFaction(factionName: String) : List[TroopSelectDto] = {
    TroopDAO.findAllForFactionByName(factionName).map(troopDo => {
      TroopSelectDto(troopDo.name, troopDo.modelType, troopDo.points, troopDo.victoryPoints)
    })
  }
}

case class TroopSelectDto(name:String, modelType: String, points: Int, victoryPoints:Int)
