package killteam.models

import killteam.parsers.{KTCsvItemDto, KTCsvTacticDto}
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Handles all data access to the tactics database
  */
object KTTacticsDao {

  /**
    * All killteam tactics
    */
  val tactics: ListBuffer[KTTacticDo] = ListBuffer()


  /**
    * Adds an item to the given faction
    *
    * @param csvItemDtothe item information's from the csv
    * @param factionDo     the faction the weapon belongs to
    * @return
    */
  def addTactic(csvTacticDto: KTCsvTacticDto): Unit = {


    // tactic is a general tactic
    if (StringUtils.isBlank(csvTacticDto.faction) && StringUtils.isBlank(csvTacticDto.specialist)) {
      Logger.info(s"Adding general tactic: ${csvTacticDto.name}")
    }

    // tactic is a faction tactic
    if (StringUtils.isNotBlank(csvTacticDto.faction) && StringUtils.isBlank(csvTacticDto.specialist)) {
      Logger.info(s"Adding tactic: ${csvTacticDto.name} for faction: ${csvTacticDto.faction}")
      // check if the faction exists
      if (KTFactionDao.findFaction(csvTacticDto.faction).isEmpty == true) {
        Logger.error(s"Cannot add tactic: ${csvTacticDto.name} for faction: ${csvTacticDto.faction} faction was not found")
        return
      }
    }

    // tactic is a specialist tactic
    if (StringUtils.isBlank(csvTacticDto.faction) && StringUtils.isNotBlank(csvTacticDto.specialist)) {
      Logger.info(s"Adding tactic: ${csvTacticDto.name} for specialist: ${csvTacticDto.specialist}")
      // check if the specialist exists
      if (KTSpecialistDao.findSpecialistByName(csvTacticDto.specialist).isEmpty == true) {
        Logger.error(s"Cannot add tactic: ${csvTacticDto.name} for specialist: ${csvTacticDto.specialist} specialist was not found")
        return
      }
    }

    tactics += KTTacticDo(faction = csvTacticDto.faction,
      name = csvTacticDto.name,
      specialist = csvTacticDto.specialist,
      specialistLevel = csvTacticDto.specialistLevel,
      commandPoints = csvTacticDto.commandPoints)
  }
}

/**
  * Represents a tactic from the tactics.csv
  *
  * @param faction         the faction the tactic belongs to
  * @param name            the name of the tactic
  * @param specialist      the name of the specialist this tactic is for when set
  * @param specialistLevel the level the specialist must have to get this tactic
  * @param commandPoints   how many command points is the tactic worth
  */
case class KTTacticDo(faction: String,
                      name: String,
                      specialist: String,
                      specialistLevel: Int,
                      commandPoints: Int)


