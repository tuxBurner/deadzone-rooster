package deadzone.parsers

import com.github.tototoshi.csv.CSVReader
import deadzone.models.CSVModels.{AbilityDto, CSVItemDto, CSVWeaponBaseDto}
import play.api.Logger

import scala.util.matching.Regex

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 15.02.17
  *         Time: 23:06
  */
object CSVItemsImporter {


  private val FACTION_HEADER = "Faction"

  private val NAME_HEADER = "Item"

  private val POINTS_HEADER = "Points"

  private val RARITY_HEADER = "Rarity"

  private val NO_UPGREADE_HEADER = "No Upgrade"



  lazy val items = importItemsFromCsv()


  
  def getItemsForFaction(faction: String) : List[CSVItemDto] = {
    items.filter(_.faction.equals(faction))
  }

  private def importItemsFromCsv(): List[CSVItemDto] = {
    val reader = CSVReader.open("conf/deadzone/items.csv")
    val dataWithHeaders = reader.allWithHeaders()
    val parsedResult = dataWithHeaders.map(parseLineMap(_)).filter(_.isDefined).map(_.get)
    parsedResult
  }

  private def parseLineMap(lineData: Map[String, String]): Option[CSVItemDto] = {


    val factionStr = lineData.get(FACTION_HEADER).get.trim
    if (factionStr.isEmpty) {
      Logger.error("CSV Item: No faction was found at line: " + lineData + " skipping it")
      return Option.empty
    }

    val nameStr = lineData.get(NAME_HEADER).get.trim
    if (nameStr.isEmpty) {
      Logger.error("CSV Item: No name was found at line: " + lineData + " skipping it")
      return Option.empty
    }

    val rarityStr = lineData.get(RARITY_HEADER).get.trim
    if (rarityStr.isEmpty) {
      Logger.error("CSV Item: No rarity was found at line: " + lineData + " skipping it")
      return Option.empty
    }

    var points = lineData.get(POINTS_HEADER).get.toInt


    val noUpgrade = lineData.get(NO_UPGREADE_HEADER).get.trim == "x"

    return Option.apply(CSVItemDto(factionStr, nameStr, points, rarityStr, noUpgrade))
  }
}
