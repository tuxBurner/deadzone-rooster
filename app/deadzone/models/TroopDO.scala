package deadzone.models

import deadzone.models.CSVModels.CSVTroopDto
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.mutable.ListBuffer


/**
  * Handles all data access which is related to a [[TroopDO]]
  */
object TroopDAO {


  /**
    * The internal storage of the [[TroopDO]] s
    */
  val troops: ListBuffer[TroopDO] = ListBuffer()

  /**
    * Removes all the troops from the internal list.
    */
  def clearAll(): Unit = {
    troops.clear()
  }

  /**
    * Returns all [[TroopDO]] which are from the given faction
    *
    * @param factionName name of the faction to look for
    * @return a [[List]] of [[TroopDO]] of the faction.
    */
  def findAllForFactionByName(factionName: String): List[TroopDO] = {
    troops
      .filter(_.faction.name == factionName)
      .toList
  }

  /**
    * Finds the [[TroopDO]] by the given factionName and troop name
    *
    * @param factionName name of the faction the [[TroopDO]] belongs to.
    * @param name        the name of the troop
    * @return [[Option]] which is empty when not found.
    */
  def findByFactionAndName(factionName: String, name: String): Option[TroopDO] = {
    findAllForFactionByName(factionName)
      .find(_.soldierDto.name == name)
  }

  /**
    * Finds all troops with an army special in the database
    *
    * @return
    */
  def findAllWithArmySpecials(): List[TroopDO] = {
    troops
      .filter(troop => StringUtils.isNotBlank(troop.soldierDto.armySpecial))
      .sortBy(_.faction.name)
      .toList
  }

  /**
    * Adds a troop to the database from the csv information's
    *
    * @param csvTroopDto the csv information'S for this new troop
    * @param factionDo   the faction the new troop belongs to
    * @return
    */
  def addFromCSVSoldierDto(csvTroopDto: CSVTroopDto, factionDo: FactionDO): TroopDO = {
    Logger.info("Creating troop: " + csvTroopDto.name + " for faction: " + factionDo.name)


    val defaultWeapons = findAllDefaultWeaponsForTroop(csvTroopDto)


    // find the allowed weapon types
    val allowedWeaponTypes = csvTroopDto.weaponTypes
      .map(
        weaponTypeName => {
          WeaponTypeDAO.findOrCreateTypeByName(weaponTypeName)
        }
      )
      .toList

    // find the default items
    val defaultItems = findAllDefaultItemsForTroop(csvTroopDto)


    val defaultTroopAbilities = csvTroopDto.abilities
      .map(DefaultTroopAbilityDAO.createDefaultAbilityForTroop(_))
      .filter(_.isDefined)
      .map(_.get)

    val troopDO = new TroopDO(
      soldierDto = csvTroopDto,
      faction = factionDo,
      defaultWeapons = defaultWeapons,
      allowedWeaponTypes = allowedWeaponTypes,
      defaultItems = defaultItems,
      defaultTroopAbilities = defaultTroopAbilities

    )

    troops += troopDO

    troopDO
  }

  /**
    * Finds all default weapons for the given troop
    *
    * @param csvTroopDto the troop from the csv import
    * @return
    */
  private def findAllDefaultWeaponsForTroop(csvTroopDto: CSVTroopDto): List[WeaponDO] = {
   csvTroopDto.defaultWeaponNames.flatMap(weaponName => {
     val weapon = WeaponDAO.findByNameAndFactionNameAndAllowedTypes(weaponName, csvTroopDto.faction, csvTroopDto.weaponTypes.toList)
     if (weapon.isEmpty) {
       Logger.error(s"Could not add default weapon ${weaponName} to troop: ${csvTroopDto.name} faction: ${csvTroopDto.faction} was not found in the db")
     }

     // check if the weapon is a linked weapon
     if (StringUtils.isNotBlank(weapon.get.linkedName)) {
       WeaponDAO.findByFactionAndLinkedName(csvTroopDto.faction, weapon.get.linkedName)
         .map(Some(_))
     } else {
       List(weapon)
     }
   }).flatten
  }

  /**
    * Finds all the default item the troop has.
    *
    * @param csvTroopDto the troop from the csv import.
    * @return
    */
  private def findAllDefaultItemsForTroop(csvTroopDto: CSVTroopDto): List[ItemDO] = {
    csvTroopDto.defaultItems
      .map(
        itemName => {
          val itemDo = ItemDAO.findByNameAndFactionName(itemName, csvTroopDto.faction)
          if (itemDo.isEmpty == true) {
            Logger.error(s"Troop: ${csvTroopDto.name} in faction: ${csvTroopDto.faction} cannot find item: ${itemName} in DB.")
          }
          itemDo
        }
      )
      .filter(_.isDefined)
      .map(_.get)
  }

}

/**
  * Class holding all information's about a troop.
  *
  * @param soldierDto            the csv import entry of the soldier.
  * @param faction               the faction the soldier belongs to.
  * @param defaultWeapons        the default weapons for this troop
  * @param defaultTroopAbilities the default abilities of this troop
  * @param allowedWeaponTypes    all the weapon types which are allowed for this troop
  * @param defaultItems          the default items of this troop
  */
case class TroopDO(soldierDto: CSVTroopDto,
                   faction: FactionDO,
                   defaultWeapons: List[WeaponDO] = List(),
                   defaultTroopAbilities: List[DefaultTroopAbilityDO] = List(),
                   allowedWeaponTypes: List[WeaponTypeDO] = List(),
                   defaultItems: List[ItemDO] = List())