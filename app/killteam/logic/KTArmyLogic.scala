package killteam.logic

import java.util.UUID

import killteam.models._
import org.apache.commons.lang3.StringUtils
import play.api.Logger

object KTArmyLogic {


  def addTroopToArmy(factionName: String, troopName: String, armyDto: KTArmyDto): KTArmyDto = {

    val troopDo = KTTroopDao.getTroopByFactionAndName(factionName, troopName)
    troopDo.map(troop => {


      val defaultLoadOut = KTLoadoutDao.getDefaultLoadout(troopName, factionName)
      if (defaultLoadOut.isEmpty) {
        Logger.error(s"No default loadout found for troop: $troopName from faction: $factionName")
        return armyDto
      }

      Logger.info(s"Adding toop: $troopName from faction: $factionName to the army")


      val loadout = loadoutDoToDto(defaultLoadOut.get)

      val troopStats = KTTroopStats(movement = troop.movement,
        fightStat = troop.fightStat,
        shootStat = troop.shootStat,
        strength = troop.strength,
        resistance = troop.resistance,
        lifePoints = troop.lifePoints,
        attacks = troop.attacks,
        moral = troop.moral,
        armor = troop.armor)

      val abilities = troop.abilities.map(ability => KTAbilityDto(ability.name))
        .toList
        .sortBy(_.name)

      val newTroop = KTArmyTroopDto(uuid = UUID.randomUUID().toString,
        name = troopName,
        faction = factionName,
        stats = troopStats,
        loadout = loadout,
        amount = 1,
        points = troop.points,
        totalPoints = calculateTroopPoints(troop.points, List(), loadout),
        abilities = abilities)

      val newTroops = armyDto.troops :+ newTroop

      armyDto.copy(faction = factionName, points = calculateArmyPoints(newTroops), troops = newTroops)
    }).getOrElse({
      Logger.warn(s"Could not add troop: $troopName from faction: $factionName it was not found")
      armyDto
    })
  }

  /**
    * Gets all possible loadout for the troop
    *
    * @param troopName  the name of the troop
    * @param factioName the name of the faction of the troop
    * @return
    */
  def getPossibleLoadoutsForTroop(troopName: String, factioName: String): List[KTLoadoutDto] = {
    KTLoadoutDao.getLoadoutsByTroopAndName(troopName, factioName)
      .map(loadoutDoToDto(_))
  }

  /**
    * Transforms the given [[KTWeaponDo]] to a [[List]] of [[KTWeaponDto]] and sorts them by there name
    *
    * @param weaponDos the dos to convert
    * @return
    */
  private def weaponDosToSortedDtos(weaponDos: Set[KTWeaponDo]): List[KTWeaponDto] = {
    weaponDos
      .map(weaponDoToDto(_))
      .toList
      .sortBy(_.name)
  }

  /**
    * Converts a [[KTWeaponDo]] to its corresponding [[KTWeaponDto]]
    *
    * @param weaponDo the weapon do to convert
    * @return
    */
  private def weaponDoToDto(weaponDo: KTWeaponDo): KTWeaponDto = {
    KTWeaponDto(name = weaponDo.name,
      points = weaponDo.points,
      range = weaponDo.range,
      weaponType = weaponDo.weaponType,
      strength = weaponDo.strength,
      puncture = weaponDo.puncture,
      damage = weaponDo.damage,
      linkedWeapon = weaponDo.linkedWeapon)
  }

  /**
    * Transforms the given [[KTItemDo]]s to a [[List]] of [[KTItemDto]] and sorts them by there name
    *
    * @param itemDos the dos to convert
    * @return
    */
  private def itemDosToSortedDtos(itemDos: Set[KTItemDo]): List[KTItemDto] = {
    itemDos
      .map(itemDoToDto(_))
      .toList
      .sortBy(_.name)
  }

  /**
    * Converts a [[KTItemDo]] to its corresponding [[KTItemDto]]
    *
    * @param itemDo the item to convert
    * @return
    */
  private def itemDoToDto(itemDo: KTItemDo): KTItemDto = {
    KTItemDto(name = itemDo.name,
      points = itemDo.points)
  }

  /**
    * Converts a [[KTLoadoutDo]] to its corresponding [[KTLoadoutDto]]
    *
    * @param loadoutDo the loadout to convert
    * @return
    */
  private def loadoutDoToDto(loadoutDo: KTLoadoutDo): KTLoadoutDto = {
    val weapons = weaponDosToSortedDtos(loadoutDo.weapons)
    val items = itemDosToSortedDtos(loadoutDo.items)
    KTLoadoutDto(name = loadoutDo.name,
      weapons = weapons,
      items = items)
  }


  /**
    * Removes the given troop from the army
    *
    * @param uuid    the uuid of the troop to remove from the army
    * @param armyDto the army from which to remove the troop
    * @return
    */
  def removeTroopFromArmy(uuid: String, armyDto: KTArmyDto): KTArmyDto = {
    Logger.info(s"Removing troop: $uuid from army")

    val newTroops = armyDto.troops.filter(_.uuid != uuid)

    val newFaction = if (newTroops.isEmpty) {
      Logger.info("No more troops in the army removing the faction")
      StringUtils.EMPTY
    } else {
      armyDto.faction
    }

    armyDto.copy(faction = newFaction, points = calculateArmyPoints(newTroops), troops = newTroops)
  }


  /**
    * Calculates all points for an army
    *
    * @param troops the troops in the army where to calculate the points for
    * @return
    */
  def calculateArmyPoints(troops: List[KTArmyTroopDto]): Int = {
    troops.map(troop => troop.totalPoints * troop.amount).sum
  }

  /**
    * Calculates the points of a troop
    *
    * @param basePoints the base points the troop has
    * @param items      the items the troop has equiped
    * @param weapons    the weapons the troop has equiped
    * @return
    */
  def calculateTroopPoints(basePoints: Int, items: List[KTItemDto], loadout: KTLoadoutDto): Int = {
    val itemsPoints = items.map(_.points).sum
    val loadoutItemsPoints = loadout.items.map(_.points).sum

    val weaponPoints = loadout.weapons.map(_.points).sum

    basePoints + itemsPoints + loadoutItemsPoints + weaponPoints
  }

  /**
    * Gets all leaders from the troop
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getLeadersFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops.filter(_.specialist == "Anführer")
  }

  /**
    * Gets all specialists from the army which are not a leader
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getSpecialistsFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops.filter(troop => StringUtils.isNotBlank(troop.specialist) && troop.specialist != "Anführer")
  }

  /**
    * Gets all troops which are normal troops in the army
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getNormalTroopsFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops.filter(troop => StringUtils.isBlank(troop.specialist))
  }

}

case class KTArmyDto(name: String,
                     faction: String = "",
                     points: Int = 0,
                     troops: List[KTArmyTroopDto] = List())

case class KTArmyTroopDto(uuid: String,
                          name: String,
                          faction: String,
                          stats: KTTroopStats,
                          loadout: KTLoadoutDto,
                          amount: Int,
                          points: Int,
                          totalPoints: Int,
                          abilities: List[KTAbilityDto],
                          items: List[KTItemDto] = List(),
                          specialist: String = "")

case class KTTroopStats(movement: Int,
                        fightStat: Int,
                        shootStat: Int,
                        strength: Int,
                        resistance: Int,
                        lifePoints: Int,
                        attacks: Int,
                        moral: Int,
                        armor: Int)

case class KTItemDto(name: String,
                     points: Int)


case class KTWeaponDto(name: String,
                       points: Int,
                       range: Int,
                       weaponType: String,
                       strength: String,
                       puncture: Int,
                       damage: String,
                       linkedWeapon: String)

case class KTAbilityDto(name: String)

/**
  * Loadout a troop can get
  *
  * @param name    the name of the loadout
  * @param weapons the weapons of the loadout
  * @param items   the items of the loadout
  */
case class KTLoadoutDto(name: String,
                        weapons: List[KTWeaponDto],
                        items: List[KTItemDto])
