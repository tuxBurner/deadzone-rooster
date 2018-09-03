package killteam.parsers

import deadzone.parsers.{CSVDataEmptyException, CSVDataParser}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}


/**
  * Parses the items.csv from the killteam csv and generates the data from it
  */
@Singleton
class KTCSVItemParser @Inject()(configuration: Configuration) extends CSVDataParser(configuration) {


  /**
    * The parsed items
    */
  private var items = List[KTCsvItemDto]()

  private val CSV_HEADER_FRACTION = "Fraktion"

  private val CSV_HEADER_NAME = "Ausrüstung"

  private val CSV_HEADER_POINTS = "Punkte"

  /**
    * Gets all items for the given faction
    *
    * @param factionName the name of the faction
    * @return
    */
  def getItemsForFaction(factionName: String): List[KTCsvItemDto] = {
    items.filter(_.factions.exists(_ == factionName))
  }


  /**
    * Refresh all the data
    */
  def refresh(): Unit = {
    items = importItemsFromCsvs()
  }

  /**
    * Imports the items from the items.csv
    *
    * @return
    */
  private def importItemsFromCsvs(): List[KTCsvItemDto] = {
    val dataWithHeaders = readCsvFile("items.csv","killteam")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  /**
    * Parse a single line of the csv to a [[KTCsvItemDto]]
    *
    * @param lineData the data of the csv in a map
    * @return
    */
  private def parseLineMap(lineData: Map[String, String]): Option[KTCsvItemDto] = {

    try {
      val parsedItem = KTCsvItemDto(factions = getSetFromLine(CSV_HEADER_FRACTION, lineData),
        name = getDataFromLine(CSV_HEADER_NAME, lineData),
        points = getIntFromLine(CSV_HEADER_POINTS, lineData))
      //Logger.info(s"Parsed troop: $parsedTroop from line $lineData")
      Some(parsedItem)

    } catch {
      case c: CSVDataEmptyException => None
    }
  }
}


/**
  * Represents a item from the items.csv
  *
  * @param faction the faction the item belongs to
  * @param name    the name of the item
  * @param points  how many points is the item worth
  */
case class KTCsvItemDto(factions: Set[String],
                        name: String,
                        points: Int)


