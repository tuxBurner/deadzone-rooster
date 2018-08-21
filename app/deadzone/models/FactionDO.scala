package deadzone.models

import play.api.Logger

import scala.collection.mutable.ListBuffer


/**
  * Handles the factions data access.
  */
object FactionDAO {


  /**
    * The internal data storage for the [[FactionDO]]
    */
  val factions: ListBuffer[FactionDO] = ListBuffer()

  /**
    * Clears all factions from this 
    */
  def clearAll(): Unit = {
    factions.clear()
  }


  /**
    * Returns all [[FactionDO]] sorted by there names.
    *
    * @return
    */
  def getAll(): List[FactionDO] = {
    factions.sortBy(_.name).toList
  }


  /**
    * Adds a new faction to the database.
    *
    * @param name the name of the faction
    * @return
    */
  def findOrAddFaction(name: String): FactionDO = {
    Logger.info("Adding Faction: " + name + " to database")

    factions
      .find(_.name.equals(name))
      .getOrElse({
        val factionDo = FactionDO(name)
        factions += factionDo
        factionDo
      })
  }
}


/**
  * Holds the information of a faction.
  *
  * @param name   the name of the faction.
  * @param troops all the troops in the faction.
  */
case class FactionDO(name: String, troops: List[TroopDO] = List())