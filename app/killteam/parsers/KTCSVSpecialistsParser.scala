package killteam.parsers

import deadzone.parsers.{CSVDataEmptyException, CSVDataParser}
import javax.inject.{Inject, Singleton}
import play.api.Configuration


/**
  * Parses the loadout.csv from the killteam csv and generates the data from it
  */
@Singleton
class KTCSVSpecialistsParser @Inject()(configuration: Configuration) extends CSVDataParser(configuration) {


  /**
    * The parsed specialists
    */
  private var specialists = List[KTCsvSpceialistDto]()

  private val CSV_HEADER_SPECIALIST = "Spezialist"

  private val CSV_HEADER_LEVEL = "Level"

  private val CSV_HEADER_REQUIRE = "Bedingung"

  private val CSV_HEADER_NAME = "Name"


  def refresh(): Unit = {
    specialists = importSpecialistsFromCsvs()
  }

  /**
    * Imports the troops from the specialists.csv
    *
    * @return
    */
  private def importSpecialistsFromCsvs(): List[KTCsvSpceialistDto] = {
    val dataWithHeaders = readCsvFile("killteam/specialists.csv")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  /**
    * Parse a single line of the csv to a [[KTCsvWeaponDto]]
    *
    * @param lineData the data of the csv in a map
    * @return
    */
  private def parseLineMap(lineData: Map[String, String]): Option[KTCsvSpceialistDto] = {

    try {
      val parsedSpecialist = KTCsvSpceialistDto(specialist = getDataFromLine(CSV_HEADER_SPECIALIST, lineData),
        level = getIntFromLine(CSV_HEADER_LEVEL, lineData),
        require = getDataFromLine(CSV_HEADER_REQUIRE, lineData, true),
        name = getDataFromLine(CSV_HEADER_NAME, lineData)
      )
      //Logger.info(s"Parsed troop: $parsedTroop from line $lineData")
      Some(parsedSpecialist)

    } catch {
      case c: CSVDataEmptyException => None
    }
  }
}


/**
  * Represents a specialist from the specialists.csv
  *
  * @param specialist the name of the specialist
  * @param name       the name of the special the specialist has
  * @param require    the name of the reacquired special to acquire this one
  * @param level      the level of the special
  */
case class KTCsvSpceialistDto(specialist: String,
                              name: String,
                              require: String,
                              level: Int)


