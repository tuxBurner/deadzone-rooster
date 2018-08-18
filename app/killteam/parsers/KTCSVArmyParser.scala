package killteam.parsers

import deadzone.parsers.{CSVDataEmptyException, CSVDataParser}
import javax.inject.{Inject, Singleton}
import play.api.Configuration


/**
  * Parses the armies.csv from the killteam csv and generates the data from it
  */
@Singleton
class KTCSVArmyParser @Inject()(configuration: Configuration) extends CSVDataParser(configuration) {


  /**
    * The parsed troops
    */
  private var troops = List[KTCsvTroopDto]()

  private val CSV_HEADER_FRACTION = "Fraktion"

  private val CSV_HEADER_NAME = "Name"

  private val CSV_HEADER_POINTS = "Punkte"

  private val CSV_HEADER_MOVEMENT = "B"

  private val CSV_HEADER_FIGHTSTAT = "KG"

  private val CSV_HEADER_SHOOTSTAT = "BF"

  private val CSV_HEADER_STRENGTH = "S"

  private val CSV_HEADER_RESISTANCE = "W"

  private val CSV_HEADER_LIFEPOINTS = "LP"

  private val CSV_HEADER_ATTACKS = "A"

  private val CSV_HEADER_MORAL = "MW"

  private val CSV_HEADER_ARMOR = "RW"

  private val CSV_HEADER_MAX_IN_ARMY = "Max"

  private val CSV_HEADER_ITEMS = "Ausrüstungsoption"

  private val CSV_HEADER_ABILITIES = "Fähigkeiten"

  private val CSV_HEADER_SPECIALIST = "Spezialisten"

  private val CSV_HEADER_KEY_WORDS = "Schlüsselwörter"


  def refresh(): Unit = {
    troops = importTroopsFromCsvs()
  }

  /**
    * Imports the troops from the armies.csv
    *
    * @return
    */
  private def importTroopsFromCsvs(): List[KTCsvTroopDto] = {
    val dataWithHeaders = readCsvFile("killteam/armies.csv")
    dataWithHeaders.map(parseLineMap(_)).flatten
  }

  /**
    * Parse a single line of the csv to a KTCsvTroopDto
    *
    * @param lineData the data of the csv in a map
    * @return
    */
  private def parseLineMap(lineData: Map[String, String]): Option[KTCsvTroopDto] = {

    try {
      val parsedTroop = KTCsvTroopDto(faction = getDataFromLine(CSV_HEADER_FRACTION, lineData),
        name = getDataFromLine(CSV_HEADER_NAME, lineData),
        points = getIntFromLine(CSV_HEADER_POINTS, lineData),
        movement = getIntFromLine(CSV_HEADER_MOVEMENT, lineData),
        fightStat = getIntFromLine(CSV_HEADER_FIGHTSTAT, lineData),
        shootStat = getIntFromLine(CSV_HEADER_SHOOTSTAT, lineData),
        strength = getIntFromLine(CSV_HEADER_STRENGTH, lineData),
        resistance = getIntFromLine(CSV_HEADER_RESISTANCE, lineData),
        lifePoints = getIntFromLine(CSV_HEADER_LIFEPOINTS, lineData),
        attacks = getIntFromLine(CSV_HEADER_ATTACKS, lineData),
        moral = getIntFromLine(CSV_HEADER_MORAL, lineData),
        armor = getIntFromLine(CSV_HEADER_ARMOR, lineData),
        maxInArmy = getIntFromLine(CSV_HEADER_MAX_IN_ARMY, lineData, true),
        items = getSetFromLine(CSV_HEADER_ITEMS, lineData, true),
        abilities = getSetFromLine(CSV_HEADER_ABILITIES, lineData, true),
        specialist = getSetFromLine(CSV_HEADER_SPECIALIST, lineData, true),
        keyWords = getSetFromLine(CSV_HEADER_KEY_WORDS, lineData, true)
      )
      //Logger.info(s"Parsed troop: $parsedTroop from line $lineData")
      Some(parsedTroop)

    } catch {
      case c: CSVDataEmptyException => None
    }
  }
}


/**
  * Represents a troop from the armies.csv
  *
  * @param faction    the faction the troop belongs to
  * @param name       the name of the troop
  * @param points     how many points is the troop worth
  * @param movement   how far can the troop move
  * @param fightStat  the fight stat the troop has
  * @param shootStat  how good can this troop shot
  * @param strength   the strength of the troop
  * @param resistance how many resistance does the troop have
  * @param lifePoints how many lifepoints does the troop have
  * @param attacks    how many attacks has the troop
  * @param moral      the moral of the troop
  * @param armor      how good is the armor
  * @param maxInArmy  maximum in an army
  * @param items      the items the troop may use
  * @param abilities  the abilities the troop has
  * @param specialist what kind of specialist can the troop be
  * @param keyWords   the key words which belong to the troop
  */
case class KTCsvTroopDto(faction: String,
                         name: String,
                         points: Int,
                         movement: Int,
                         fightStat: Int,
                         shootStat: Int,
                         strength: Int,
                         resistance: Int,
                         lifePoints: Int,
                         attacks: Int,
                         moral: Int,
                         armor: Int,
                         maxInArmy: Int,
                         items: Set[String],
                         abilities: Set[String],
                         specialist: Set[String],
                         keyWords: Set[String]
                        )
