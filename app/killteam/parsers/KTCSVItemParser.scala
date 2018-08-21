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
    * @param faction the name of the faction
    * @return
    */
  def getItemsForFaction(faction: String): List[KTCsvItemDto] = {
    items.groupBy(_.faction).getOrElse(faction, {
      Logger.warn(s"KT Could not find any item for faction: $faction")
      List()
    })
  }


  /**
    * Refresh all the data
    */
  def refresh(): Unit = {
    items = importItemsromCsvs()
  }

  /**
    * Imports the troops from the armies.csv
    *
    * @return
    */
  private def importItemsromCsvs(): List[KTCsvItemDto] = {
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
      val parsedItem = KTCsvItemDto(faction = getDataFromLine(CSV_HEADER_FRACTION, lineData),
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
case class KTCsvItemDto(faction: String,
                        name: String,
                        points: Int)


