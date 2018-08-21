package killteam.models

import killteam.parsers.KTCsvTroopDto
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Handles all data access to the troop database
  */
object KTTroopDao {
  /**
    * All killteam troops
    */
  val troops: ListBuffer[KTTroopDo] = ListBuffer()


  /**
    * Returns all troops of a faction
    * @param factionName the name of the faction
    * @return
    */
  def getAllTroopsOfFaction(factionName: String) : Set[KTTroopDo] = {
    troops.filter(_.factionDo.name == factionName).toSet
  }

  /**
    * Gets the troop by the faction and its name
    * @param factionName the name of the faction
    * @param troopName the name of the troop
    * @return
    */
  def getTroopByFactionAndName(factionName: String, troopName: String) : Option[KTTroopDo] = {
    troops.find(troopDo => troopDo.factionDo.name == factionName && troopDo.name == troopName)
  }

  /**
    * Adds a troop to the given faction
    *
    * @param csvWeaponDto the weapon information's from the csv
    * @param factionDo    the faction the weapon belongs to
    * @return
    */
  def addTroopToFaction(csvTroopDo: KTCsvTroopDto, factionDo: KTFactionDo): KTTroopDo = {

    Logger.info(s"KT adding troop: ${csvTroopDo.name} to faction: ${factionDo.name}")

    val items: ListBuffer[KTItemDo] = ListBuffer()
    csvTroopDo.items.foreach(itemName => {
      if(StringUtils.isNotBlank(itemName)) {
        val itemDo = KTItemsDao.getItemByNameAndFaction(itemName, factionDo)
        if (itemDo.isEmpty) {
          Logger.error(s"KT cannot add item: $itemName to troop: ${csvTroopDo.name} faction: ${factionDo.name} because it was not found")
        } else {
          items += itemDo.get
        }
      }
    })

    val abilities: ListBuffer[KTAbilityDo] = ListBuffer()
    csvTroopDo.abilities.foreach(abilityName => {
      if(StringUtils.isNotBlank(abilityName)) {
        val abilityDo = KTAbilityDao.getOrAddAbilityToFaction(abilityName,factionDo)
        abilities += abilityDo
      }
    })

    val specialists: ListBuffer[KTSpecialistDo] = ListBuffer()
    csvTroopDo.specialist.foreach(specialistName => {
      if(StringUtils.isNotBlank(specialistName )) {
        val specialistDo = KTSpecialistDao.findSpecialistByName(specialistName)
        if (specialistDo.isEmpty) {
          Logger.error(s"KT cannot add specialist: $specialistName to troop: ${csvTroopDo.name} faction: ${factionDo.name} because it was not found")
        } else {
          specialists += specialistDo.get
        }
      }
    })
                      
    val troopDo = KTTroopDo(factionDo = factionDo,
      name = csvTroopDo.name,
      points = csvTroopDo.points,
      movement = csvTroopDo.movement,
      fightStat = csvTroopDo.fightStat,
      shootStat = csvTroopDo.shootStat,
      strength = csvTroopDo.strength,
      resistance = csvTroopDo.resistance,
      lifePoints = csvTroopDo.lifePoints,
      attacks = csvTroopDo.attacks,
      moral = csvTroopDo.moral,
      armor = csvTroopDo.armor,
      maxInArmy = csvTroopDo.maxInArmy,
      items = items.toSet,
      abilities = abilities.toSet,
      specialist = specialists.toSet,
      keyWords = csvTroopDo.keyWords
    )

    troops += troopDo

    troopDo
  }
}

/**
  * Represents a troop
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
case class KTTroopDo(factionDo: KTFactionDo,
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
                     items: Set[KTItemDo],
                     abilities: Set[KTAbilityDo],
                     specialist: Set[KTSpecialistDo],
                     keyWords: Set[String]
                    )

