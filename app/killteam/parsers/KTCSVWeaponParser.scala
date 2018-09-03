package killteam.parsers

import deadzone.parsers.{CSVDataEmptyException, CSVDataParser, CSVWeaponImporter}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}


/**
  * Parses the weapons.csv from the killteam csv and generates the data from it
  */
@Singleton
class KTCSVWeaponParser @Inject()(configuration: Configuration, csvWeaponImporter: CSVWeaponImporter) extends CSVDataParser(configuration) {


  /**
    * The parsed weapons
    */
  private var weapons = List[KTCsvWeaponDto]()

  private val CSV_HEADER_FRACTION = "Fraktion"

  private val CSV_HEADER_NAME = "Waffe"

  private val CSV_HEADER_POINTS = "Punkte"

  private val CSV_HEADER_RANGE = "RW"

  private val CSV_HEADER_TYPE = "Typ"

  private val CSV_HEADER_STRENGTH = "S"

  private val CSV_HEADER_PUNCTURE = "DS"

  private val CSV_HEADER_DAMAGE = "SW"

  private val CSV_HEADER_LINKED_WEAPON = "Kombiwaffe"


  /**
    * Gets all weapons for the given faction
    *
    * @param factionName the name of the faction
    * @return
    */
  def getWeaponsForFaction(factionName: String): List[KTCsvWeaponDto] = {
    weapons.filter(_.factions.exists(_ == factionName))
  }


  /**
    * Refresh all the data
    */
  def refresh(): Unit = {
    weapons = importWeaonsFromCsvs()
  }

  /**
    * Imports the troops from the armies.csv
    *
    * @return
    */
  private def importWeaonsFromCsvs(): List[KTCsvWeaponDto] = {
    val dataWithHeaders = readCsvFile("weapons.csv","killteam")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  /**
    * Parse a single line of the csv to a [[KTCsvWeaponDto]]
    *
    * @param lineData the data of the csv in a map
    * @return
    */
  private def parseLineMap(lineData: Map[String, String]): Option[KTCsvWeaponDto] = {

    try {
      val parsedWeapon = KTCsvWeaponDto(factions = getSetFromLine(CSV_HEADER_FRACTION, lineData),
        name = getDataFromLine(CSV_HEADER_NAME, lineData),
        points = getIntFromLine(CSV_HEADER_POINTS, lineData),
        range = getIntFromLine(CSV_HEADER_RANGE, lineData),
        weaponType = getDataFromLine(CSV_HEADER_TYPE, lineData),
        strength = getDataFromLine(CSV_HEADER_STRENGTH, lineData),
        puncture = getIntFromLine(CSV_HEADER_PUNCTURE, lineData),
        damage = getDataFromLine(CSV_HEADER_DAMAGE, lineData),
        linkedWeapon = getDataFromLine(CSV_HEADER_LINKED_WEAPON, lineData, true)
      )
      //Logger.info(s"Parsed troop: $parsedTroop from line $lineData")
      Some(parsedWeapon)

    } catch {
      case c: CSVDataEmptyException => None
    }
  }
}


/**
  * Represents a weapon from the weapons.csv
  *
  * @param faction      the faction the weapon belongs to
  * @param name         the name of the weapon
  * @param points       how many points is the weapon worth
  * @param range        the range the weapon has
  * @param strength     the strength of the troop
  * @param puncture     the puncture value of the weapon
  * @param damage       the damage the weapon causes
  * @param linkedWeapon when set the weapon is linked with another weapon
  */
case class KTCsvWeaponDto(factions: Set[String],
                          name: String,
                          points: Int,
                          range: Int,
                          weaponType: String,
                          strength: String,
                          puncture: Int,
                          damage: String,
                          linkedWeapon: String)


