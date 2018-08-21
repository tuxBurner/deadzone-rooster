package killteam.logic

import java.util.UUID

import killteam.models._
import org.apache.commons.lang3.StringUtils
import play.api.Logger

object KTArmyLogic {


  /**
    * Adds a troop to the given army
    *
    * @param factionName the faction name of the troop to add
    * @param troopName   the name of the troop to add
    * @param armyDto     the army wher to add the troop
    * @return
    */
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
        level = 1,
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
    * Gathers all possible config options for the troop
    *
    * @param uuid    the uuid of the troop
    * @param armyDto the army where the troop is in
    * @return
    */
  def getPossibleConfigurationOptionsForTroop(uuid: String, armyDto: KTArmyDto): Option[KTTroopOptionsDto] = {
    getTroopFromArmyByUUID(uuid, armyDto)
      .map(troopDto => {
        val loadOutsForTroop = getPossibleLoadoutsForTroop(troopDto)
          .map(loadout => {
            val selected = loadout.name == troopDto.loadout.name
            KTOptionLoadout(selected = selected, loadout = loadout)
          })


        val itemsForTroop = getPossibleItemsForTroop(troopDto)

        val specialists = getPossibleSpecialistsForTroop(troopDto)

        Some(KTTroopOptionsDto(loadoutOptions = loadOutsForTroop,
          items = itemsForTroop,
          specialists = specialists))
      })
      .getOrElse({
        Logger.error(s"Troop $uuid not found in army")
        None
      })
  }

  def getPossibleSpecialistsForTroop(troopDto: KTArmyTroopDto): List[KTSpecialistOptionDto] = {
    KTTroopDao.getTroopByFactionAndName(troopName = troopDto.name, factionName = troopDto.faction)
      .map(troopDo => {
        troopDo.specialists.map(specialistDo => {
          // check if the specialist is currently selected by the troop
          val selected = troopDto.specialist.exists(_.name == specialistDo.name)
          KTSpecialistOptionDto(selected = selected,
            name = specialistDo.name)
        })
          .toList
          .sortBy(_.name)
      })
      .getOrElse({
        Logger.error(s"Troop: ${troopDto.name} not found in faction: ${troopDto.faction}")
        List()
      })
  }

  /**
    * Gets all possible loadout for the troop
    *
    * @param troopDto the  troop to get loadout for
    * @return
    */
  private def getPossibleLoadoutsForTroop(troopDto: KTArmyTroopDto): List[KTLoadoutDto] = {
    KTLoadoutDao.getLoadoutsByTroopAndName(troopName = troopDto.name, factionName = troopDto.faction)
      .map(loadoutDoToDto(_))
      .sortBy(_.name)
  }

  /**
    * Gets all possible items from the troop
    *
    * @param troopDto the troop to get the items for
    * @return
    */
  private def getPossibleItemsForTroop(troopDto: KTArmyTroopDto): List[KTItemDto] = {
    KTTroopDao.getTroopByFactionAndName(troopName = troopDto.name, factionName = troopDto.faction)
      .map(troopDo => itemDosToSortedDtos(troopDo.items))
      .getOrElse({
        Logger.error(s"Troop: ${troopDto.name} not found in faction: ${troopDto.faction}")
        List()
      })

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
    val weaponPoints = weapons.map(_.points).sum

    val items = itemDosToSortedDtos(loadoutDo.items)
    val itemsPoints = items.map(_.points).sum

    KTLoadoutDto(name = loadoutDo.name,
      points = weaponPoints + itemsPoints,
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
    * Finds a troop in the army by its uuid
    *
    * @param uuid    the uuid to find the troop
    * @param armyDto the army to search for the troop
    * @return
    */
  private def getTroopFromArmyByUUID(uuid: String, armyDto: KTArmyDto): Option[KTArmyTroopDto] = {
    armyDto.troops.find(_.uuid == uuid)
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
    * @param loadout    the current loadout of the troop
    * @return
    */
  def calculateTroopPoints(basePoints: Int, items: List[KTItemDto], loadout: KTLoadoutDto): Int = {
    val itemsPoints = items.map(_.points).sum
    basePoints + itemsPoints + loadout.points
  }

  /**
    * Gets all leaders from the troop
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getLeadersFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops.filter(troop => troop.specialist.isDefined && troop.specialist.get.name == "Anführer")
  }

  /**
    * Gets all specialists from the army which are not a leader
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getSpecialistsFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops.filter(troop => troop.specialist.isDefined && troop.specialist.get.name != "Anführer")
  }

  /**
    * Gets all troops which are normal troops in the army
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getNormalTroopsFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops.filter(_.specialist.isEmpty)
  }

}

/**
  * Represents a killteam army
  *
  * @param name    the name of the army
  * @param faction the faction of the army
  * @param points  how many points is the army worth
  * @param troops  the troops in the army
  */
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
                          level: Int,
                          totalPoints: Int,
                          abilities: List[KTAbilityDto],
                          items: List[KTItemDto] = List(),
                          specialist: Option[KTSpecialistDo] = None)

case class KTTroopStats(movement: Int,
                        fightStat: Int,
                        shootStat: Int,
                        strength: Int,
                        resistance: Int,
                        lifePoints: Int,
                        attacks: Int,
                        moral: Int,
                        armor: Int)

/**
  * Represents an item
  *
  * @param name   the name of the item
  * @param points how many points id the item worth
  */
case class KTItemDto(name: String,
                     points: Int)

case class KTSpecialistOptionDto(selected: Boolean,
                                 name: String)

/**
  * Represents a specialist of a troop
  *
  * @param name     the name of the specialist
  * @param specials the specials the specialist currently has
  */
case class KTSpecialistDto(name: String,
                           specials: List[KTSpecialDto])

/**
  * Represents a special a troop can have
  *
  * @param name  the name of the special
  * @param level the level when this special was aquired
  */
case class KTSpecialDto(name: String,
                        level: Int)


/**
  * Represents a weapon
  *
  * @param name         the name of the weapon
  * @param points       how many points is the weapon worth
  * @param range        the range of the weapon
  * @param weaponType   the type of the weapon
  * @param strength     the strength of the weapon
  * @param puncture     the puncture of the weapon
  * @param damage       the damage of the weapon
  * @param linkedWeapon when the weapon is linked weapon this is set
  */
case class KTWeaponDto(name: String,
                       points: Int,
                       range: Int,
                       weaponType: String,
                       strength: String,
                       puncture: Int,
                       damage: String,
                       linkedWeapon: String)

/**
  * An ability a troop can have
  *
  * @param name the name of the ability
  */
case class KTAbilityDto(name: String)

/**
  * Contains all options a troop can have
  *
  * @param loadoutOptions all possible loadouts options for the troop
  * @param items          all possible items the troop can equip
  * @param specialists    the specialists this troop can be
  */
case class KTTroopOptionsDto(loadoutOptions: List[KTOptionLoadout],
                             items: List[KTItemDto],
                             specialists: List[KTSpecialistOptionDto])

/**
  * Loadout option
  *
  * @param selected true when the loadout is currently selected in the troop
  * @param loadout  the loadout itself
  */
case class KTOptionLoadout(selected: Boolean,
                           loadout: KTLoadoutDto)

/**
  * Loadout a troop can get
  *
  * @param name    the name of the loadout
  * @param weapons the weapons of the loadout
  * @param items   the items of the loadout
  * @param points  how many points is the loadout worth
  */
case class KTLoadoutDto(name: String,
                        points: Int,
                        weapons: List[KTWeaponDto],
                        items: List[KTItemDto])
