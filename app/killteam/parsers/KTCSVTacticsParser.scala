package killteam.parsers

import deadzone.parsers.{CSVDataEmptyException, CSVDataParser}
import javax.inject.{Inject, Singleton}
import killteam.parsers
import play.api.{Configuration, Logger}


/**
  * Parses the tactics.csv from the killteam csv and generates the data from it
  */
@Singleton
class KTCSVTacticsParser @Inject()(configuration: Configuration) extends CSVDataParser(configuration) {


  /**
    * The parsed tactics
    */
  private var tactics = List[KTCsvTacticDto]()

  private val CSV_HEADER_FRACTION = "Fraktion"

  private val CSV_HEADER_NAME = "Name"

  private val CSV_HEADER_COMMAND_POINTS = "BP"

  private val CSV_HEADER_SPECIALIST = "Spezialist"

  private val CSV_HEADER_LEVEL = "Stufe"


  /**
    * Returns all parsed csv tactics
    *
    * @return
    */
  def getAllTactics(): List[KTCsvTacticDto] = {
    tactics
  }


  /**
    * Refresh all the data
    */
  def refresh(): Unit = {
    tactics = importTactcicsFromCsvs()
  }

  /**
    * Imports the troops from the armies.csv
    *
    * @return
    */
  private def importTactcicsFromCsvs(): List[KTCsvTacticDto] = {
    val dataWithHeaders = readCsvFile("tactics.csv", "killteam")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  /**
    * Parse a single line of the csv to a [[KTCsvTacticDto]]
    *
    * @param lineData the data of the csv in a map
    * @return
    */
  private def parseLineMap(lineData: Map[String, String]): Option[KTCsvTacticDto] = {

    try {
      val parsedItem = parsers.KTCsvTacticDto(faction = getDataFromLine(CSV_HEADER_FRACTION, lineData, true),
        name = getDataFromLine(CSV_HEADER_NAME, lineData),
        commandPoints = getIntFromLine(CSV_HEADER_COMMAND_POINTS, lineData),
        specialist = getDataFromLine(CSV_HEADER_SPECIALIST, lineData, true),
        specialistLevel = getIntFromLine(CSV_HEADER_LEVEL, lineData, true))
      Some(parsedItem)

    } catch {
      case c: CSVDataEmptyException => None
    }
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
case class KTCsvTacticDto(faction: String,
                          name: String,
                          specialist: String,
                          specialistLevel: Int,
                          commandPoints: Int)




