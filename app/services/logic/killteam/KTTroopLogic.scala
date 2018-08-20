package services.logic.killteam

import models.killteam.KTTroopDao

object KTTroopLogic {


  /**
    * Gets all select troops dto for the given faction
    * @param faction the faction to get the troops for
    * @return
    */
  def getAllSelectTroopsForFaction(faction: String): List[KTTroopSelectDto] = {
    KTTroopDao.getAllTroopsOfFaction(factionName = faction)
      .toList
      .sortBy(_.name)
      .map(troopDo => KTTroopSelectDto(name = troopDo.name, points = troopDo.points))
  }

}

/**
  * Represents a troop for the drop down select in the frontend
  *
  * @param name   the name of the troop
  * @param points the troop points
  */
case class KTTroopSelectDto(name: String,
                            points: Int,
                            imageUrl: String = "")
