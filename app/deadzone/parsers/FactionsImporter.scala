package deadzone.parsers

import java.io.File

import com.github.tototoshi.csv.CSVReader
import deadzone.models.ModelType
import deadzone.models.Models.SoldierDto
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 16.02.17
  *         Time: 14:39
  */
object FactionsImporter {


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

  private lazy val soldiers = importSoldiersFromCsvs()

  def getAllAvaibleFactions: List[String] = soldiers.map(_.faction).toSet.toList

  def getSoldierForFaction(factionName: String) = soldiers.filter(_.faction == factionName)

  private def importSoldiersFromCsvs(): List[SoldierDto] = {

    // iterate over all files
    val armiesConfFolder = new File("conf/deadzone/armies")

    armiesConfFolder.listFiles.filter(f => (f.isFile && f.getName.endsWith(".csv"))).flatMap(file => {
      val armyName = file.getName.replaceAll(".csv", "")

      Logger.debug("Reading configuration for factions: " + armyName + " from file: " + file.getAbsolutePath)

      // process the file
      val reader = CSVReader.open(file)
      val dataWithHeaders = reader.allWithHeaders()

      dataWithHeaders.map(parseLineMap(_, armyName)).filter(_.isDefined).map(_.get)
    }).toList
  }

  private def parseLineMap(lineData: Map[String, String], faction: String): Option[SoldierDto] = {

    val name = lineData.get(NAME_HEADER).get
    if (name.isEmpty == true) {
      Logger.error("No name given for soldier for faction: " + faction + " in line: " + lineData)
      return Option.empty
    }


    val pointsData = lineData.get(POINTS_HEADER).get
    if (pointsData.isEmpty == true) {
      Logger.error("No point given for soldier: " + name + " for faction: " + faction + " in line: " + lineData)
      return Option.empty
    }
    val points = pointsData.toInt

    val typeData = lineData.get(TYPE_HEADER).get
    if (typeData.isEmpty == true) {
      Logger.error("No type given for soldier: " + name + " for faction: " + faction + " in line: " + lineData)
      return Option.empty
    }

    val matchedTyp = typeData match {
      case "L" => ModelType.Leader
      case "T" => ModelType.Troop
      case "S" => ModelType.Speacialist
      case "V" => ModelType.Vehicle
      case "C" => ModelType.Character
      case _ => None
    }
    if (matchedTyp == None) {
      Logger.error("No type matched for: " + typeData + " for soldier: " + name + " for faction: " + faction + " in line: " + lineData)
      return Option.empty
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

    val sizeValue = lineData.get(SIZE_HEADER).get.trim.toInt
    val armour = lineData.get(ARMOUR_HEADER).get.trim.toInt
    val victoryPoints = lineData.get(VICTORY_POINTS_HEADER).get.trim.toInt

    val abilitiesData = lineData.get(ABILITIES_HEADER).get.trim
    val abilities = WeaponImporter.parseAbilities(abilitiesData)

    val weaponsEquipmentData = lineData.get(WEAPONS_HEADER).get.trim
    val defaultWeaponNames = ListBuffer.empty[String]

    if (StringUtils.isBlank(weaponsEquipmentData) == false) {
      val splitWeapons = weaponsEquipmentData.split(',')
      splitWeapons.foreach(weaponInfo => {
        // check if the weapon is available
        val factionWeapons = WeaponImporter.getWeaponsForFaction(faction)
        val trimmedWeaponName = weaponInfo.trim
        factionWeapons.find(_.name.equals(trimmedWeaponName))
          .map(matchedWeapon => {
            defaultWeaponNames += matchedWeapon.name
          })
          .getOrElse(
            Logger.error("Weapon: " + trimmedWeaponName + " not found for troop: " + name + " faction: " + faction)
          )
      })
    }


    Option.apply(SoldierDto(faction, name, points, matchedTyp.asInstanceOf[ModelType.Value], speed, shootRange, fightValue, surviveValue, sizeValue, armour, victoryPoints, abilities, defaultWeaponNames.toList))
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
