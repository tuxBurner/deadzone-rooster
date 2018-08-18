package models.killteam

import play.api.Logger

import scala.collection.mutable.ListBuffer

object KTFactionDao {

  /**
    * All killteam factions
    */
  val factions: ListBuffer[KTFactionDo] = ListBuffer()


  /**
    * Deletes all factions
    */
  def deleteAll():Unit = {
    factions.clear()
  }

  /**
    * Adds a new faction to the database.
    *
    * @param name the name of the faction
    * @return
    */
  def findOrAddFaction(name: String): KTFactionDo = {
    Logger.info("KT adding Faction: " + name + " to database")

    factions
      .find(_.name.equals(name))
      .getOrElse({
        val factionDo = KTFactionDo(name)
        factions += factionDo
        factionDo
      })
  }

}

case class KTFactionDo(name: String)
