package deadzone.parsers


import javax.inject.{Inject, Singleton}

import deadzone.models.CSVModels.CSVItemDto
import deadzone.models.ItemRarity
import play.api.{Configuration, Logger}

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 15.02.17
  *         Time: 23:06
  */
@Singleton()
class CSVItemsImporter @Inject()(configuration: Configuration) extends CSVDataParser(configuration) {


  private val FACTION_HEADER = "Faction"

  private val NAME_HEADER = "Item"

  private val POINTS_HEADER = "Points"

  private val RARITY_HEADER = "Rarity"

  private val NO_UPGREADE_HEADER = "No Upgrade"


  var items = importItemsFromCsv()


  def refresh(): Unit = {
    items = importItemsFromCsv()
  }

  def getItemsForFaction(faction: String): List[CSVItemDto] = {
    items.filter(_.faction.equals(faction))
  }

  private def importItemsFromCsv(): List[CSVItemDto] = {
    val dataWithHeaders = readCsvFile("deadzone/items.csv")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  private def parseLineMap(lineData: Map[String, String]): List[CSVItemDto] = {


    val factionsStr = lineData.get(FACTION_HEADER).get.trim
    if (factionsStr.isEmpty) {
      Logger.error(s"CSV Item: No faction was found at line: ${lineData} skipping it")
      return List.empty
    }

    val nameStr = lineData.get(NAME_HEADER).get.trim
    if (nameStr.isEmpty) {
      Logger.error(s"CSV Item: No name was found at line: ${lineData} skipping it")
      return List.empty
    }

    val rarityStr = lineData.get(RARITY_HEADER).get.trim
    if (rarityStr.isEmpty) {
      Logger.error(s"CSV Item: No rarity was found at line: ${lineData} skipping it")
      return List.empty
    }

    val matchedRarity = rarityStr match {
      case "Common" => ItemRarity.Common
      case "Rare" => ItemRarity.Rare
      case "Unique" => ItemRarity.Unique
      case _ => None
    }

    if(matchedRarity == None) {
      Logger.error(s"CSV Item: No mazched rarity was found at line: ${lineData} skipping it")
      return List.empty
    }

    val points = lineData.get(POINTS_HEADER).get.toInt


    val noUpgrade = lineData.get(NO_UPGREADE_HEADER).get.trim == "x"

    splitStringByCommaAndTrim(factionsStr).map(factionStr => {
      CSVItemDto(faction = factionStr,
        name = nameStr,
        points = points,
        rarity = matchedRarity.toString,
        noUpgrade = noUpgrade)
    }).toList
  }
}
