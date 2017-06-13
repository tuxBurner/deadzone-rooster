package services.logic

import java.util.UUID

import models._

import scala.collection.JavaConversions._

/**
  * Created by tuxburner on 12.06.17.
  */
object ArmyLogic {

  def addTroopToArmy(factionName: String, troopName: String, army: ArmyDto): ArmyDto = {
    val troopDo = TroopDAO.findByFactionAndName(factionName, troopName)

    val newTroop = troopDoToArmyTroopDto(troopDo)

    val newTroops: List[ArmyTroopDto] = army.troops :+ newTroop
    val armyPoints = newTroops.map(_.points).sum

    army.copy(troops = newTroops,faction = factionName, points = armyPoints)
  }

  def removeTroopFromArmy(uuid: String, army: ArmyDto): ArmyDto = {
    val newTroops = army.troops.filter(_.uuid != uuid)
    val armyPoints = newTroops.map(_.points).sum
    val faction = if(newTroops.size == 0) "" else army.faction
    army.copy(troops = newTroops, faction = faction, points = armyPoints)
  }

  def troopDoToArmyTroopDto(troopDo: TroopDO): ArmyTroopDto = {

    val troopAbilities = troopDo.defaultTroopAbilities.toList.map(abilityDo => ArmyAbilityDto(abilityDo.ability.name, abilityDo.defaultValue))
    val weapons = troopDo.defaultWeapons.toList.map(weaponDoToWeaponDto(_))

    val points = troopDo.points + troopDo.defaultWeapons.toList.map(_.points).sum
    val victoryPoints = troopDo.victoryPoints + troopDo.defaultWeapons.toList.map(_.victoryPoints).sum

    val weaponTypes = troopDo.allowedWeaponTypes.map(_.name).toList

    val items = troopDo.defaultItems.map(itemDoToItemDto(_)).toList

    val uuid = UUID.randomUUID().toString

    ArmyTroopDto(uuid,
      troopDo.faction.name,
      troopDo.name,
      troopDo.modelType,
      points,
      victoryPoints,
      troopDo.speed,
      troopDo.sprint,
      troopDo.armour,
      troopDo.size,
      troopDo.shoot,
      troopDo.fight,
      troopDo.survive,
      troopAbilities,
      weapons,
      items,
      weaponTypes,
      troopDo.recon,
      troopDo.armySpecial)
  }

  def getWeaponsAndItemsForTroop(uuid: String, army: ArmyDto) : ArmyTroopWeaponsItemsDto = {
    val weapons = getWeaponsForTroop(uuid,army)
    val items = getItemsForTroop(uuid,army)
    ArmyTroopWeaponsItemsDto(weapons,items)
  }


  /**
    * Gets all avaible weapon options for the given troop
    * @param uuid
    * @param army
    * @return
    */
  def getWeaponsForTroop(uuid: String, army: ArmyDto): Map[String,List[ArmyWeaponDto]] = {
    val troopDto = army.troops.find(_.uuid == uuid).get
    val weapons = WeaponDAO.findByFactionAndTypes(troopDto.faction, troopDto.allowedWeaponTypes).toList

    val rangedWeaopns = weapons.filter(weaponDo => weaponDo.shootRange != 0 && weaponDo.free == false).map(weaponDoToWeaponDto(_))
    val fightWeapons = weapons.filter(weaponDo => weaponDo.shootRange == 0 && weaponDo.free == false).map(weaponDoToWeaponDto(_))
    val freeWeapons = weapons.filter(_.free == true).map(weaponDoToWeaponDto(_))


    Map("ranged" -> rangedWeaopns, "fight" -> fightWeapons, "free" -> freeWeapons)
  }

  /**
    * Gets the items for the given troop
    * @param uuid
    * @param army
    * @return
    */
  def getItemsForTroop(uuid:String, army: ArmyDto) : List[ArmyItemDto] = {
    ItemDAO.findAllItemsForFaction(army.faction).map(itemDoToItemDto(_))
  }

  /**
    * Transforms an item from the backend to an item for the frontend.
    * @param itemDo
    * @return
    */
  def itemDoToItemDto(itemDo: ItemDO) : ArmyItemDto = {
    ArmyItemDto(itemDo.name, itemDo.points, itemDo.rarity)
  }

  def weaponDoToWeaponDto(weaponDo: WeaponDO): ArmyWeaponDto = {
    val abilities = weaponDo.defaultWeaponAbilities.toList.map(abilityDo => ArmyAbilityDto(abilityDo.ability.name, abilityDo.defaultValue))
    ArmyWeaponDto(weaponDo.name,
      weaponDo.points,
      weaponDo.shootRange,
      weaponDo.armorPircing,
      weaponDo.victoryPoints,
      abilities)
  }

}

case class ArmyDto(name: String, faction:String = "", points: Int = 0, troops: List[ArmyTroopDto] = List())

case class ArmyAbilityDto(name: String, defaultVal: Int)

case class ArmyTroopDto(uuid: String,
                        faction: String,
                        name: String,
                        modelType: String,
                        points: Int,
                        victoryPoints: Int,
                        speed: Int,
                        sprint: Int,
                        armour: Int,
                        size: Int,
                        shoot: Int,
                        fight: Int,
                        survive: Int,
                        abilities: List[ArmyAbilityDto],
                        weapons: List[ArmyWeaponDto],
                        items: List[ArmyItemDto],
                        allowedWeaponTypes: List[String],
                        recon: Int,
                        armySpecial: String)

case class ArmyWeaponDto(name: String,
                         points: Int,
                         shootRange: Int,
                         armorPircing: Int,
                         victoryPoints: Int,
                         abilities: List[ArmyAbilityDto])

case class ArmyItemDto(name: String, points: Int, rarity: String)

case class ArmyTroopWeaponsItemsDto(weapons: Map[String,List[ArmyWeaponDto]], items: List[ArmyItemDto])