package services.logic.killteam

import java.util.UUID

import models.killteam.{KTLoadoutDao, KTTroopDao}
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

      // get all items from the loadout
      val itemsFromLoadout = defaultLoadOut.get.items
        .map(itemDo => {
          KTItemDto(name = itemDo.name,
            points = itemDo.points)
        })
        .toList
        .sortBy(_.name)


      // gets all weapons from the loadout
      val weapons = defaultLoadOut.get.weapons
        .map(weaponDo => {
          KTWeaponDto(name = weaponDo.name,
            points = weaponDo.points,
            range = weaponDo.range,
            weaponType = weaponDo.weaponType,
            strength = weaponDo.strength,
            puncture = weaponDo.puncture,
            damage = weaponDo.damage,
            linkedWeapon = weaponDo.linkedWeapon)
        })
        .toList
        .sortBy(_.name)

      val troopStats = KTTroopStats(movement = troop.movement,
        fightStat = troop.fightStat,
        shootStat = troop.shootStat,
        strength = troop.strength,
        resistance = troop.resistance,
        lifePoints = troop.lifePoints,
        attacks = troop.attacks,
        moral = troop.moral,
        armor = troop.armor)


      val newTroop = KTArmyTroopDto(uuid = UUID.randomUUID().toString,
        name = troopName,
        faction = factionName,
        stats = troopStats,
        loadoutName = defaultLoadOut.get.name,
        points = troop.points,
        totalPoints = calculateTroopPoints(troop.points, itemsFromLoadout, List(), weapons),
        amount = 1,
        itemsFromLoadout = itemsFromLoadout,
        items = List(),
        weapons = weapons
      )

      armyDto.copy(faction = factionName, points = calculateArmyPoints(armyDto), troops = armyDto.troops :+ newTroop)
    }).getOrElse({
      Logger.warn(s"Could not add troop: $troopName from faction: $factionName it was not found")
      armyDto
    })
  }

  def calculateArmyPoints(armyDto: KTArmyDto): Int = {
    armyDto.troops.map(troop => troop.totalPoints * troop.amount).sum
  }

  def calculateTroopPoints(basePoints: Int, itemsByLoadout: List[KTItemDto], items: List[KTItemDto], weapons: List[KTWeaponDto]): Int = {
    val loadutItemsPoints = itemsByLoadout.map(_.points).sum
    val itemsPoints = items.map(_.points).sum
    val weaponPoints = weapons.map(_.points).sum

    basePoints + loadutItemsPoints + itemsPoints + weaponPoints
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
                          loadoutName: String,
                          amount: Int,
                          points: Int,
                          totalPoints: Int,
                          itemsFromLoadout: List[KTItemDto],
                          items: List[KTItemDto],
                          weapons: List[KTWeaponDto],
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
