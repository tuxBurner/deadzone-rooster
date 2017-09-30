package models

import play.api.Logger

import scala.collection.mutable.ListBuffer


object FactionDAO {


  val factions: ListBuffer[FactionDO] = ListBuffer()


  def getAll(): List[FactionDO] = {
    factions.sortBy(_.name).toList
  }


  def addFaction(name: String): FactionDO = {
    Logger.info("Adding Faction: " + name + " to database")

    val factionDo = FactionDO(name)
    factions += factionDo
    factionDo
  }
}


case class FactionDO(name: String, troops: List[TroopDO] = List())