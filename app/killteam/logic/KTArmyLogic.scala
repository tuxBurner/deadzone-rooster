package killteam.logic

import java.util.UUID

import killteam.models._
import org.apache.commons.lang3.StringUtils
import play.api.Logger

object KTArmyLogic {


  /**
    * Adds a troop to the given army
    *
    * @param factionName    the faction name of the troop to add
    * @param troopName      the name of the troop to add
    * @param specialistName the name of the specialist to set at the troop
    * @param armyDto        the army wher to add the troop
    * @return
    */
  def addTroopToArmy(factionName: String, troopName: String, specialistName: String, armyDto: KTArmyDto): KTArmyDto = {

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

      val specialist = if (StringUtils.isBlank(specialistName)) {
        None
      } else {
        KTSpecialistLogic.getSpecialistForTroop(specialistName)
      }

      val newTroop = KTArmyTroopDto(uuid = UUID.randomUUID().toString,
        name = troopName,
        faction = factionName,
        stats = troopStats,
        loadout = loadout,
        amount = 1,
        points = troop.points,
        level = 1,
        totalPoints = calculateTroopPoints(troop.points, List(), loadout, 1),
        abilities = abilities,
        specialist = specialist)

      val newTroops = armyDto.troops :+ newTroop

      val newArmy = armyDto.copy(faction = factionName, points = calculateArmyPoints(newTroops), troops = newTroops)
      newArmy.copy(tactics = KTTacticsLogic.getTacticsForArmy(newArmy))
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

        val specialistsOption = KTSpecialistLogic.getSpecialistOptionForTroop(troopDto)

        Some(KTTroopOptionsDto(loadoutOptions = loadOutsForTroop,
          items = itemsForTroop,
          specialistsOption = specialistsOption))
      })
      .getOrElse({
        Logger.error(s"Troop $uuid not found in army")
        None
      })
  }


  /**
    * Sets the given loadout at the troop
    *
    * @param loadoutName the name of the loadout to set
    * @param uuid        the uuid of the troop to set the loadout
    * @param armyDto     the army containing the troop
    * @return
    */
  def setLoadoutAtTroop(loadoutName: String, uuid: String, armyDto: KTArmyDto): KTArmyDto = {

    Logger.info(s"Setting loadout: $loadoutName at troop: $uuid")

    getTroopFromArmyByUUIDAndPerformChanges(uuid = uuid, armyDto = armyDto, troopDto => {
      KTLoadoutDao.getLoadoutByTroopAndName(troopName = troopDto.name, factionName = troopDto.faction, loadoutName)
        .map(loadoutDo => {
          val newLoadout = loadoutDoToDto(loadoutDo)
          val troopDtoWithNewLoadout = troopDto.copy(loadout = newLoadout)
          Some(troopDtoWithNewLoadout)
        })
        .getOrElse({
          Logger.error(s"Cannot find loadout: $loadoutName for troop: ${troopDto.name} faction: ${troopDto.faction}")
          None
        })
    })
  }

  /**
    * Replaces the current troop with the new one and recalculates the points of the army
    *
    * @param updatedTroop the troop which to replace
    * @param armyDto      the army containing the troop
    * @return
    */
  def updateArmyWithTroop(updatedTroop: KTArmyTroopDto, armyDto: KTArmyDto): KTArmyDto = {
    val troopIndex = armyDto.troops.indexWhere(_.uuid == updatedTroop.uuid)
    val updatedArmyTroops = armyDto.troops.updated(troopIndex, updatedTroop)
    val armyWithNewTroops = armyDto.copy(troops = updatedArmyTroops, points = calculateArmyPoints(updatedArmyTroops))
    armyWithNewTroops.copy(tactics = KTTacticsLogic.getTacticsForArmy(armyWithNewTroops))
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

    val newArmy = armyDto.copy(faction = newFaction, points = calculateArmyPoints(newTroops), troops = newTroops)
    newArmy.copy(tactics = KTTacticsLogic.getTacticsForArmy(newArmy))
  }

  /**
    * Use this to get a troop by its uuid and perform changes and get an updated army by it
    *
    * @param uuid           the uuid of the troop to change some data on it
    * @param armyDto        the army containing the troop
    * @param successHandler what todo when the troop was found in the army
    * @return
    */
  def getTroopFromArmyByUUIDAndPerformChanges(uuid: String, armyDto: KTArmyDto, successHandler: (KTArmyTroopDto) => Option[KTArmyTroopDto]): KTArmyDto = {
    getTroopFromArmyByUUID(uuid = uuid, armyDto = armyDto)
      .map(troopDto => {
        successHandler(troopDto)
          .map(updatedTroop => updateArmyWithTroop(updatedTroop = updatedTroop.copy(totalPoints = calculateTroopPoints(updatedTroop.points, updatedTroop.items, updatedTroop.loadout, updatedTroop.level)), armyDto = armyDto))
          .getOrElse(armyDto)
      })
      .getOrElse({
        Logger.error(s"Troop $uuid not found in army")
        armyDto
      })

  }


  /**
    * Finds a troop in the army by its uuid and handles changes int the successHandler when found
    *
    * @param uuid    the uuid to find the troop
    * @param armyDto the army to search for the troop
    * @return
    */
  def getTroopFromArmyByUUID(uuid: String, armyDto: KTArmyDto): Option[KTArmyTroopDto] = {
    armyDto
      .troops
      .find(_.uuid == uuid)
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
    * @param level      the level of the troop
    * @return
    */
  def calculateTroopPoints(basePoints: Int, items: List[KTItemDto], loadout: KTLoadoutDto, level: Int): Int = {
    val itemsPoints = items.map(_.points).sum
    val levelPoints = level match {
      case 2 => 4
      case 3 => 8
      case 4 => 12
      case _ => 0
    }
    basePoints + itemsPoints + loadout.points + levelPoints
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
  * @param tactics all the tactics the army has
  */
case class KTArmyDto(name: String = "",
                     faction: String = "",
                     points: Int = 0,
                     troops: List[KTArmyTroopDto] = List(),
                     tactics: Map[ETacticType, List[KTTacticDto]] = Map())


/**
  * Represents a troop in the army
  *
  * @param uuid        the uuid of the troop
  * @param name        the name of the troop
  * @param faction     the faction of the troop
  * @param stats       the stats the troop has
  * @param loadout     the loadout the tropp is equiped with
  * @param amount      how many of the troop are in the army
  * @param points      the base points of the troop
  * @param level       the level of the troop
  * @param totalPoints the total points the troop cost
  * @param abilities   the abilities the troop has
  * @param items       the items the troop is equiped with
  * @param specialist  when set the troop is a specialist
  */
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
                          specialist: Option[KTSpecialistTroopDto] = None)

/**
  * The stats of a troop
  *
  * @param movement   how far can the troop move
  * @param fightStat  the fight stat of the troop
  * @param shootStat  the shoot stat of the troop
  * @param strength   the strength of the troop
  * @param resistance the resistance of the troop
  * @param lifePoints how many lifepoints does the troop have
  * @param attacks    how many attacks does the troop have
  * @param moral      the moral of the troop
  * @param armor      the armor  of the troop
  */
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
  * @param loadoutOptions    all possible loadouts options for the troop
  * @param items             all possible items the troop can equip
  * @param specialistsOption the specialist this troop has and which specials can or are be selected
  */
case class KTTroopOptionsDto(loadoutOptions: List[KTOptionLoadout],
                             items: List[KTItemDto],
                             specialistsOption: Option[KTSpecialistOptionDto])

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
