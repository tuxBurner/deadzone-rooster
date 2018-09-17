package killteam.logic

import killteam.models.{KTTacticDo, KTTacticsDao}
import org.apache.commons.lang3.StringUtils
import play.api.Logger

/**
  * Logic for handling all the tactics
  */
object KTTacticsLogic {

  /**
    * Gets all tactics known to the roster
    *
    * @return
    */
  def getAllTactics(): Map[ETacticType, List[KTTacticDto]] = {

    val generalTactics = KTTacticsDao.getGeneralTactics().map(tacticDoToDto).sortBy(_.name)

    val specialistTactics = KTSpecialistLogic.getAllSpecialist().flatMap(specialist => {
      KTTacticsDao
        .getSpecialistTactics(specialistName = specialist.name, maxLevel = 4)
        .map(tacticDoToDto)
    }).sortBy(tactic => (tactic.specialist, tactic.specialistLevel))

    val factionTactics = KTFactionLogic.getAllFactions().flatMap(faction => {
      KTTacticsDao.getFactionTactics(factionName = faction.name)
        .map(tacticDoToDto)
    }).sortBy(tactic => (tactic.faction, tactic.name))

    Map(ETacticType.GENERAL -> generalTactics, ETacticType.FACTION -> factionTactics, ETacticType.SPECIALIST -> specialistTactics)
  }

  /**
    * Gets all tactics which are avaible for the army
    *
    * @param armyDto
    * @return
    */
  def getTacticsForArmy(armyDto: KTArmyDto): Map[ETacticType, List[KTTacticDto]] = {
    Logger.info(s"Collecting tactics for the army")

    val generalTactics = if (armyDto.troops.isEmpty == false) {
      KTTacticsDao
        .getGeneralTactics()
        .map(tacticDoToDto)
    } else {
      List()
    }

    val factionTactics = getTacticsForFaction(armyDto.faction).sortBy(tactic => (tactic.faction, tactic.name))


    val specialistTactics = armyDto
      .troops
      .filter(_.specialist.isDefined) // get all troops having a specialist
      .groupBy(_.specialist.get.name) // group the troops by the specialists name
      .map(entry => entry._2.maxBy(_.level)).flatMap(troopWithSpecial => KTTacticsDao.getSpecialistTactics(troopWithSpecial.specialist.get.name, troopWithSpecial.level))
      .map(tacticDoToDto)
      .toList
      .sortBy(tactic => (tactic.specialist, tactic.specialistLevel))


    Map(ETacticType.GENERAL -> generalTactics, ETacticType.FACTION -> factionTactics, ETacticType.SPECIALIST -> specialistTactics)
  }


  /**
    * Gets all faction tactics
    *
    * @param factionName the name of the faction to get the tactics for
    * @return
    */
  def getTacticsForFaction(factionName: String): List[KTTacticDto] = {
    if (StringUtils.isBlank(factionName)) {
      List()
    } else {
      KTTacticsDao
        .getFactionTactics(factionName)
        .map(tacticDoToDto)
    }
  }

  /**
    * Converts the given do to a dto
    *
    * @param tacticDo the tactic do to convert
    * @return
    */
  private def tacticDoToDto(tacticDo: KTTacticDo): KTTacticDto = {
    KTTacticDto(name = tacticDo.name,
      faction = tacticDo.faction,
      specialist = tacticDo.specialist,
      specialistLevel = tacticDo.specialistLevel,
      commandPoints = tacticDo.commandPoints
    )
  }

}


/**
  * Represents a tactic
  *
  * @param faction         the faction the tactic belongs to
  * @param name            the name of the tactic
  * @param specialist      the name of the specialist this tactic is for when set
  * @param specialistLevel the level the specialist must have to get this tactic
  * @param commandPoints   how many command points is the tactic worth
  */
case class KTTacticDto(faction: String,
                       name: String,
                       specialist: String,
                       specialistLevel: Int,
                       commandPoints: Int)