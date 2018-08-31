package killteam.logic

import killteam.models.{KTTacticDo, KTTacticsDao}
import org.apache.commons.lang3.StringUtils
import play.api.Logger

/**
  * Logic for handling all the tactics
  */
object KTTacticsLogic {

  /**
    * Gets all tactics which are avaible for the army
    *
    * @param armyDto
    * @return
    */
  def getTacticsForArmy(armyDto: KTArmyDto): List[KTTacticDto] = {
    Logger.info(s"Collecting tactics for the army")

    val generalTactics = KTTacticsDao
      .getGeneralTactics()
      .map(tacticDoToDto)

    val factionTactics = if (StringUtils.isBlank(armyDto.faction)) {
      List()
    } else {
      KTTacticsDao
        .getFactionTactics(armyDto.faction)
        .map(tacticDoToDto)
    }


    val specialistTactics = armyDto
      .troops
      .filter(_.specialist.isDefined) // get all troops having a specialist
      .groupBy(_.specialist.get.name) // group the troops by the specialists name
      .map(entry => entry._2.maxBy(_.level)).flatMap(troopWithSpecial => KTTacticsDao.getSpecialistTactics(troopWithSpecial.specialist.get.name, troopWithSpecial.level))
      .map(tacticDoToDto)


    generalTactics ++ factionTactics ++ specialistTactics
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