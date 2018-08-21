package deadzone.parsers

import javax.inject.{Inject, Singleton}

import deadzone.models.CSVModels.CSVTroopDto
import deadzone.models.ModelType
import org.apache.commons.lang3.StringUtils
import play.api.{Configuration, Logger}

import scala.collection.mutable.ListBuffer

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 16.02.17
  *         Time: 14:39
  */
@Singleton
class CSVArmyImporter @Inject()(configuration: Configuration, csvWeaponImporter: CSVWeaponImporter) extends CSVDataParser(configuration) {


  private val NAME_HEADER = "Name"

  private val POINTS_HEADER = "Points"

  private val TYPE_HEADER = "Type"

  private val SPEED_HEADER = "Speed"

  private val SHOOT_HEADER = "Shoot"

  private val FIGHT_HEADER = "Fight"

  private val SURVIVE_HEADER = "Survive"

  private val SIZE_HEADER = "Size"

  private val ARMOUR_HEADER = "Armour"

  private val VICTORY_POINTS_HEADER = "VPs"

  private val ABILITIES_HEADER = "Abilities"

  private val WEAPONS_HEADER = "Weapons"

  private val WEAPON_UPGRADES_HEADER = "Weapon Upgrades"

  private val HARDPOINTS_HEADER = "Hardpoints"

  private val RECON_HEADER = "Recon"

  private val ARMY_SPECIAL_HEADER = "Army Special"

  private val ITEM_HEADER = "Equipment"

  private val FACTION_HEADER = "Faction"

  private val IMG_URL_HEADER = "Img"

  private var soldiers = importSoldiersFromCsvs()

  def refresh(): Unit = {
    soldiers = importSoldiersFromCsvs()
  }

  def getAllAvaibleFactions: List[String] = soldiers.map(_.faction).toSet.toList

  def getSoldierForFaction(factionName: String) = soldiers.filter(_.faction == factionName)


  private def importSoldiersFromCsvs(): List[CSVTroopDto] = {
    val dataWithHeaders = readCsvFile("armies.csv","deadzone")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  /**
    * Parses the line data from a csv
    *
    * @param lineData
    * @return
    */
  private def parseLineMap(lineData: Map[String, String]): List[CSVTroopDto] = {

    val factions = lineData.get(FACTION_HEADER).get
    if (factions.isEmpty == true) {
      Logger.error(s"CSV Army: No factions given in line: ${lineData}")
      return List.empty
    }

    val name = lineData.get(NAME_HEADER).get
    if (name.isEmpty == true) {
      Logger.error(s"CSV Army: No name given for soldier for faction: ${factions} in line: ${lineData}")
      return List.empty
    }


    val pointsData = lineData.get(POINTS_HEADER).get
    if (pointsData.isEmpty == true) {
      Logger.error(s"CSV Army: No point given for soldier: ${name} for faction: ${factions} in line: ${lineData}")
      return List.empty
    }
    var points = pointsData.toInt

    val typeData = lineData.get(TYPE_HEADER).get
    if (typeData.isEmpty == true) {
      Logger.error(s"CSV Army: No type given for soldier: ${name} for faction: ${factions} in line: ${lineData}")
      return List.empty
    }

    val matchedTyp = typeData match {
      case "L" => ModelType.Leader
      case "T" => ModelType.Troop
      case "S" => ModelType.Specialist
      case "V" => ModelType.Vehicle
      case "C" => ModelType.Character
      case _ => None
    }
    if (matchedTyp == None) {
      Logger.error(s"No type matched for: ${typeData} for soldier: ${name} for faction: ${factions} in line: ${lineData}")
      return List.empty
    }

    val speedData = lineData.get(SPEED_HEADER).get.trim
    val speed = speedData match {
      case "-" => (0, 0)
      case "" => (0, 0)
      case _ => {
        val splittedData = speedData.split("-")
        (splittedData(0).toInt, splittedData(1).toInt)
      }
    }

    val shootRange = parsePlusValue(lineData.get(SHOOT_HEADER).get)
    val fightValue = parsePlusValue(lineData.get(FIGHT_HEADER).get)
    val surviveValue = parsePlusValue(lineData.get(SURVIVE_HEADER).get)

    val recon = parsePlusValue(lineData.get(RECON_HEADER).get)
    val armySpecial = lineData.get(ARMY_SPECIAL_HEADER).get.trim

    val sizeValue = lineData.get(SIZE_HEADER).get.trim.toInt
    val armour = lineData.get(ARMOUR_HEADER).get.trim.toInt
    var victoryPoints = lineData.get(VICTORY_POINTS_HEADER).get.trim.toInt

    val hardPoints = csvWeaponImporter.getNumberWithDefault(lineData.get(HARDPOINTS_HEADER), 0)

    val abilitiesData = lineData.get(ABILITIES_HEADER).get.trim
    val abilities = csvWeaponImporter.parseAbilities(abilitiesData)

    val weaponsEquipmentData = lineData.get(WEAPONS_HEADER).get.trim
    val defaultWeaponNames = ListBuffer.empty[String]

    val splittedFactions = splitStringByCommaAndTrim(factions)

    if (StringUtils.isBlank(weaponsEquipmentData) == false) {
      val splitWeapons = splitStringByCommaAndTrim(weaponsEquipmentData)

      splitWeapons.foreach(weaponInfo => {

        // check if we have the weapons for the faction
        splittedFactions.foreach(faction => {

          // check if the weapon is available
          val factionWeapons = csvWeaponImporter.getWeaponsForFaction(faction)
          val trimmedWeaponName = weaponInfo.trim
          factionWeapons.find(_.name.equals(trimmedWeaponName))
            .map(matchedWeapon => {

              if(defaultWeaponNames.contains(matchedWeapon.name) == false) {
                points -= matchedWeapon.points
                victoryPoints -= matchedWeapon.victoryPoints
                defaultWeaponNames += matchedWeapon.name
              }
            })
            .getOrElse(
              Logger.error(s"Weapon: ${trimmedWeaponName} not found for troop: ${name} faction: ${faction}")
            )
        })
      })
    }
    val weaponTypes = splitStringByCommaAndTrim(lineData.get(WEAPON_UPGRADES_HEADER).get).filter(_ != "")

    val items = splitStringByCommaAndTrim(lineData.get(ITEM_HEADER).get).filter(_ != "").toList

    val imgUrl = lineData.get(IMG_URL_HEADER).getOrElse("")

    splittedFactions.map(faction => {
      CSVTroopDto(faction, name, points, matchedTyp.asInstanceOf[ModelType.Value], speed, shootRange, fightValue, surviveValue, sizeValue, armour, victoryPoints, abilities, defaultWeaponNames.toList, weaponTypes, hardPoints, recon, armySpecial, items, imgUrl)
    }).toList


  }


  private def parsePlusValue(data: String): Int = {
    val trimData = data.trim
    trimData match {
      case "-" => 0
      case "" => 0
      case _ => trimData.replace("+", "").toInt
    }
  }

}
