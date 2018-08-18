package killteam.parsers

import deadzone.parsers.{CSVDataEmptyException, CSVDataParser}
import javax.inject.{Inject, Singleton}
import play.api.Configuration


/**
  * Parses the loadout.csv from the killteam csv and generates the data from it
  */
@Singleton
class KTCSVLoadoutParser @Inject()(configuration: Configuration) extends CSVDataParser(configuration) {


  /**
    * The parsed loadouts
    */
  private var loadouts = List[KTCsvLoadoutDto]()

  private val CSV_HEADER_FRACTION = "Faction Keyword"

  private val CSV_HEADER_TROOP = "Name"

  private val CSV_HEADER_NAME = "Loadout"

  private val CSV_HEADER_WEAPONS = "Weapon"

  private val CSV_HEADER_ITEMS = "Ausrüstung"


  def refresh(): Unit = {
    loadouts = importLoadoutFromCsvs()
  }

  /**
    * Imports the troops from the armies.csv
    *
    * @return
    */
  private def importLoadoutFromCsvs(): List[KTCsvLoadoutDto] = {
    val dataWithHeaders = readCsvFile("killteam/loadout.csv")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  /**
    * Parse a single line of the csv to a [[KTCsvWeaponDto]]
    *
    * @param lineData the data of the csv in a map
    * @return
    */
  private def parseLineMap(lineData: Map[String, String]): Option[KTCsvLoadoutDto] = {

    try {
      val parsedLoadout = KTCsvLoadoutDto(faction = getDataFromLine(CSV_HEADER_FRACTION, lineData),
        troop = getDataFromLine(CSV_HEADER_TROOP, lineData),
        name = getDataFromLine(CSV_HEADER_NAME, lineData),
        weapons = getSetFromLine(CSV_HEADER_WEAPONS, lineData),
        items = getSetFromLine(CSV_HEADER_ITEMS, lineData, true)
      )
      //Logger.info(s"Parsed troop: $parsedTroop from line $lineData")
      Some(parsedLoadout)

    } catch {
      case c: CSVDataEmptyException => None
    }
  }
}


/**
  * Represents a loadout from the loadout.csv
  *
  * @param faction the faction the loadout belongs to
  * @param troop   the troop of the loadout
  * @param name    the name of the loadout
  * @param weapons the weapons in the loadout
  * @param items   the items in the loadout
  */
case class KTCsvLoadoutDto(faction: String,
                           troop: String,
                           name: String,
                           weapons: Set[String],
                           items: Set[String])


