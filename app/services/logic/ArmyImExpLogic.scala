package services.logic

import models.{ItemDAO, TroopDAO, WeaponDAO}
import org.apache.commons.lang3.StringUtils
import services.logic.ArmyLogic.{itemDoToItemDto, weaponDoToWeaponDto}


/**
  * Does the im exp of an army
  */
object ArmyImExpLogic {


  /**
    * Exports a list of troops in an army
    *
    * @param army the army to export
    * @return
    */
  def armyForExport(army: ArmyDto): ArmyImpExpDto = {
    val troops = army.troopsWithAmount.map(amountTroop => TroopImExpDto(faction = amountTroop.troop.faction,
      name = amountTroop.troop.name,
      weapons = amountTroop.troop.weapons.map(_.name),
      items = amountTroop.troop.items.map(_.name),
      amount = amountTroop.amount))
    ArmyImpExpDto(name = army.name, troops = troops)
  }

  /**
    * Imports an army from the data
    *
    * @param armyToImport the army to import
    * @return
    */
  def importArmy(armyToImport: ArmyImpExpDto): ArmyDto = {

    val troops = armyToImport.troops.flatMap(createTroopFromImport)
    val points = troops.map(_.troop.points).sum
    val factions = ArmyLogic.getFactionsFromArmy(troops)
    ArmyDto(armyToImport.name, factions, points, troops)
  }

  /**
    * Creates a troop with weapon and item loadout
    *
    * @param troop the troop to create
    * @return
    */
  private def createTroopFromImport(troop: TroopImExpDto): Option[ArmyAmountTroopDto] = {

    if (StringUtils.isBlank(troop.name) || StringUtils.isBlank(troop.faction)) {
      return None
    }

    val troopDoOption = TroopDAO.findByFactionAndName(troop.faction, troop.name)
    if (troopDoOption.isEmpty) {
      return None
    }

    val troopDo = troopDoOption.get

    val newWeapons = troop.weapons.map(weaponName => {
      val weaponDo = WeaponDAO.findByNameAndFactionNameAndAllowedTypes(weaponName, troop.faction, troopDo.allowedWeaponTypes.map(_.name))
      weaponDoToWeaponDto(weaponDo.get)
    })

    val newItems = troop.items.map(itemName => {
      val itemDo = ItemDAO.findByNameAndFactionName(itemName, troop.faction)
      itemDoToItemDto(itemDo.get)
    })

    val points = troopDo.soldierDto.points + newWeapons.map(_.points).sum + newItems.map(_.points).sum
    val victoryPoints = troopDo.soldierDto.victoryPoints + newWeapons.map(_.victoryPoints).sum

    val troopForAmry = ArmyLogic.troopDoToArmyTroopDto(troopDo).copy(
      points = points,
      victoryPoints = victoryPoints,
      weapons = newWeapons,
      items = newItems)

    Option.apply(ArmyAmountTroopDto(troop = troopForAmry, amount = troop.amount))

  }

  case class ArmyImpExpDto(name: String = "", troops: List[TroopImExpDto])

  case class TroopImExpDto(faction: String, name: String, weapons: List[String], items: List[String], amount: Int)

}
