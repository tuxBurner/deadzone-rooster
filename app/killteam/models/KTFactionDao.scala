package killteam.models

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
  def deleteAll(): Unit = {
    factions.clear()
  }

  /**
    * Finds the given faction by its name
    *
    * @param name the name of the faction to find
    * @return
    */
  def findFaction(name: String): Option[KTFactionDo] = {
    factions
      .find(_.name.equals(name))
  }

  /**
    * Adds a new faction to the database.
    *
    * @param name the name of the faction
    * @return
    */
  def findOrAddFaction(name: String): KTFactionDo = {
    Logger.info("KT adding Faction: " + name + " to database")

    findFaction(name)
      .getOrElse({
        val factionDo = KTFactionDo(name)
        factions += factionDo
        factionDo
      })
  }

}

case class KTFactionDo(name: String)
