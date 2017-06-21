package deadzone.parsers

import com.github.tototoshi.csv.CSVReader
import deadzone.models.CSVModels.{AbilityDto, CSVWeaponDto}
import deadzone.parsers.CSVItemsImporter.{parseLineMap, readCsvFile}
import play.api.Logger

import scala.util.matching.Regex

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 15.02.17
  *         Time: 23:06
  */
object CSVWeaponImporter extends CSVDataParser {


  private val FACTION_HEADER = "Faction"

  private val WEAPON_NAME_HEADER = "Weapon"

  private val POINTS_HEADER = "Points"

  private val VPS_HEADER = "VPs"

  private val ARMOR_PIERCING_HEADER = "AP"

  private val TYPE_HEADER = "Type"

  private val RANGE_HEADER = "Range"

  private val ABILITIES_HEADER = "Abilities"

  private val HARDPOINTS_HEADER = "Hardpoints"

  private val LINKED_WEAPON_NAME_HEADER = "LinkedName"

  // when true the free when small arms is allowed
  private val ADD_ON_HEADER ="Add On"
    
  private val NUMBER_REGEX = new Regex("(\\(.*?)\\)")

  lazy val weapons = importWeaponFromCsv()


  
  def getWeaponsForFaction(faction: String) : List[CSVWeaponDto] = {
    weapons.filter(_.faction.equals(faction))
  }

  private def importWeaponFromCsv(): List[CSVWeaponDto] = {
    val dataWithHeaders =  readCsvFile("deadzone/weapons.csv")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  private def parseLineMap(lineData: Map[String, String]): Option[CSVWeaponDto] = {

    val typeStr = lineData.get(TYPE_HEADER).get.trim
    if (typeStr.isEmpty) {
      Logger.error("CSV Weapon: No type was found at line: " + lineData + " skipping it")
      return Option.empty
    }
    val weaponTypes = typeStr.split(',').map(_.trim)

    val factionStr = lineData.get(FACTION_HEADER).get.trim
    if (factionStr.isEmpty) {
      Logger.error("CSV Weapon: No faction was found at line: " + lineData + " skipping it")
      return Option.empty
    }

    val nameStr = lineData.get(WEAPON_NAME_HEADER).get.trim
    if (nameStr.isEmpty) {
      Logger.error("CSV Weapon: No name was found at line: " + lineData + " skipping it")
      return Option.empty
    }

    val points = lineData.get(POINTS_HEADER).get.toInt
    val vps = lineData.get(VPS_HEADER).get.toInt


    val range = lineData.get(RANGE_HEADER).get.trim
    if (range.isEmpty == true) {
      Logger.error("CSV Weapon: No range was found at line: " + lineData + " skipping it")
      return Option.empty
    }

    if (range.count(_ == 'R') != 1) {
      Logger.error("CSV Weapon: Found no or multiple: R in Range for " + lineData + " skipping it")
      return Option.empty
    }

    val rangeAsInt = range.replace("R", "").replace("F", "0").toInt

    val ap = getNumberWithDefault(lineData.get(ARMOR_PIERCING_HEADER),0)
    val hp = getNumberWithDefault(lineData.get(HARDPOINTS_HEADER),0)

    val abilitiesData = lineData.get(ABILITIES_HEADER).get.trim
    val abilities = parseAbilities(abilitiesData)

    val free = lineData.get(ADD_ON_HEADER).get.trim == "x"

    val linkedName = lineData.get(LINKED_WEAPON_NAME_HEADER).get.trim

    return Option.apply(CSVWeaponDto(factionStr, nameStr, points, vps, rangeAsInt, ap, weaponTypes, hp, free, abilities, linkedName))
  }


  def getNumberWithDefault(data: Option[String], default: Int): Int = {
    data match {
      case Some(apsString) => {
        if (apsString == "") {
          default
        } else {
          apsString.replaceAll("AP", "").trim.toInt
        }
      }
      case None => default
    }
  }

   def parseAbilities(abilitiesData: String): List[AbilityDto] = {
    abilitiesData.split(',').map(abilitiyInfo => {
      val titleData = abilitiyInfo.trim
      val number = NUMBER_REGEX.findFirstIn(titleData).map(p => p.replace("(", "").replace(")", "").trim.toInt).getOrElse(0)
      val title = NUMBER_REGEX.replaceAllIn(titleData, "")

      AbilityDto(title, number)
    }).toList
  }

}
