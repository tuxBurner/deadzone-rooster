package services.logic

import models.{ItemDAO, TroopDAO, WeaponDAO}
import org.apache.commons.lang3.StringUtils
import services.logic.ArmyLogic.{itemDoToItemDto, weaponDoToWeaponDto}

import scala.collection.JavaConversions._

/**
  * Does the im exp of an army
  */
object ArmyImExpLogic {


  /**
    * Exports a list of troops in an army
    *
    * @param army
    * @return
    */
  def armyForExport(army: ArmyDto): ArmyImpExpDto = {
    val troops = army.troops.map(troop => TroopImExpDto(troop.faction, troop.name, troop.weapons.map(_.name), troop.items.map(_.name)))
    ArmyImpExpDto(army.name,troops = troops)
  }

  /**
    * Imports an army from the data
    *
    * @param armyToImport
    * @return
    */
  def importArmy(armyToImport: ArmyImpExpDto): ArmyDto = {

    val troops = armyToImport.troops.map(createTroopFromImport(_)).flatten
    val points = troops.map(_.points).sum
    val factions = ArmyLogic.getFactionsFromArmy(troops)
    ArmyDto(armyToImport.name,factions,points,troops)
  }

  /**
    * Creates a troop with weapon and item loadout
    * @param troop
    * @return
    */
  private def createTroopFromImport(troop: TroopImExpDto): Option[ArmyTroopDto] = {

    if (StringUtils.isBlank(troop.name) || StringUtils.isBlank(troop.faction)) {
      return Option.empty
    }

    val troopDo = TroopDAO.findByFactionAndName(troop.faction, troop.name)
    if (troopDo == null) {
      return Option.empty
    }

    val newWeapons = troop.weapons.map(weaponName => {
      val weaponDo = WeaponDAO.findByNameAndFactionNameAndAllowedTypes(weaponName, troop.faction, troopDo.allowedWeaponTypes.toList.map(_.name))
      weaponDoToWeaponDto(weaponDo)
    })

    val newItems = troop.items.map(itemName => {
      val itemDo = ItemDAO.findByNameAndFactionName(itemName, troop.faction)
      itemDoToItemDto(itemDo)
    })

    val points = troopDo.points + newWeapons.map(_.points).sum + newItems.map(_.points).sum
    val victoryPoints =  troopDo.victoryPoints + newWeapons.map(_.victoryPoints).sum

    val troopForAmry = ArmyLogic.troopDoToArmyTroopDto(troopDo).copy(points = points, victoryPoints = victoryPoints,weapons = newWeapons, items = newItems)

    Option.apply(troopForAmry)

  }

  case class ArmyImpExpDto(name:String = "",troops: List[TroopImExpDto])

  case class TroopImExpDto(faction: String, name: String, weapons: List[String], items: List[String])

}
