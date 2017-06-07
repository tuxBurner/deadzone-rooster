package deadzone.parsers

import play.api.Logger

/**
  * 
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 22.02.17
  *         Time: 23:06
  */
object FactionCleaner {


  /**
    *
    */
  def cleanFactionSoldiers() = {

    Logger.info("Going to clean the factions to there baselines")

    val availFactions = FactionsImporter.getAllAvaibleFactions
    availFactions.map(cleanFaction(_))

  }

  def cleanFaction(factionName : String): Unit = {

    val soldiers = FactionsImporter.getSoldierForFaction(factionName)

    

  }
}
