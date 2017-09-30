package models

import deadzone.models.CSVModels.CSVSoldierDto
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.mutable.ListBuffer


object TroopDAO {

  val troops: ListBuffer[TroopDO] = ListBuffer();

  def findAllForFactionByName(factionName: String): List[TroopDO] = {
    troops
      .filter(_.faction.name == factionName)
      .toList
  }

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
    * Adds a troop to the databse from the csv information's
    *
    * @param soldierDto
    * @param factionDo
    * @return
    */
  def addFromCSVSoldierDto(soldierDto: CSVSoldierDto, factionDo: FactionDO): TroopDO = {
    Logger.info("Creating troop: " + soldierDto.name + " for faction: " + factionDo.name)


    // find the weapons
    val weapons = soldierDto.defaultWeaponNames
      .map(
        weaponName => {
          val weapon = WeaponDAO.findByNameAndFactionAndAllowedTypes(weaponName, factionDo, soldierDto.weaponTypes.toList)
          if (weapon.isEmpty) {
            Logger.error("Could not add default weapon " + weaponName + " to troop: " + soldierDto.name + " faction: " + factionDo.name + " was not found in the db")
          }
          weapon
        }
      )
      .filter(_.isDefined)
      .map(_.get)


    // find the allowed weapon types
    val allowedWeaponTypes = soldierDto.weaponTypes
      .map(
        weaponTypeName => {
          WeaponTypeDAO.findOrCreateTypeByName(weaponTypeName)
        }
      )
      .toList

    // find the default items
    val defaultItems = soldierDto.defaultItems
      .map(
        itemName => {
          val itemDo = ItemDAO.findByNameAndFaction(itemName, factionDo)
          if (itemDo.isEmpty == true) {
            Logger.error("Troop: " + soldierDto.name + " in faction: " + soldierDto.faction + " cannot find item: " + itemName + " in DB.")
          }
          itemDo
        }
      )
      .filter(_.isDefined)
      .map(_.get)


    val defaultTroopAbilities = soldierDto.abilities
      .map(DefaultTroopAbilityDAO.addAbilityForTroop(_))
      .filter(_.isDefined)
      .map(_.get)

    val troopDO = new TroopDO(
      soldierDto = soldierDto,
      faction = factionDo,
      defaultWeapons = weapons,
      allowedWeaponTypes = allowedWeaponTypes,
      defaultItems = defaultItems,
      defaultTroopAbilities = defaultTroopAbilities

    )

    troops += troopDO

    troopDO
  }

}

case class TroopDO(soldierDto: CSVSoldierDto,
                   faction: FactionDO,
                   defaultWeapons: List[WeaponDO] = List(),
                   defaultTroopAbilities: List[DefaultTroopAbilityDO] = List(),
                   allowedWeaponTypes: List[WeaponTypeDO] = List(),
                   defaultItems: List[ItemDO] = List())