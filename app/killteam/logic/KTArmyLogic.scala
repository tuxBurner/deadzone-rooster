package killteam.logic

import java.util.UUID

import killteam.models._
import org.apache.commons.lang3.StringUtils
import play.api.Logger

object KTArmyLogic {


  /**
    * Sets the amount of the troop in the army
    *
    * @param uuid    the uuid of the troop to set the amount for
    * @param amount  the amount to set
    * @param armyDto the army where the troop is in
    * @return
    */
  def setAmountOfTroopInArmy(uuid: String, amount: Int, armyDto: KTArmyDto): KTArmyDto = {

    if (amount <= 0) {
      Logger.error(s"Amount is: $amount must be bigger than 1")
      return armyDto
    }

    getTroopFromArmyByUUIDAndPerformChanges(uuid = uuid, armyDto = armyDto, troopDto => {

      // check if maximum is already reached
      if(troopDto.maxInArmy != 0) {
        val foundInArmy = KTTroopLogic.countAmountInArmy(troopDto = troopDto, armyDto = armyDto)

        if(foundInArmy >= troopDto.maxInArmy && amount > troopDto.maxInArmy) {
          Logger.error(s"Found: $foundInArmy of troop: ${troopDto.name} max allowed are: ${troopDto.maxInArmy}")
          return armyDto
        }
      }

      // check if there may be more specialists in the army
      if (troopDto.specialist.isDefined) {
        // count how many specialist are in the army
        val specialists = KTSpecialistLogic.countSpecialistsInArmy(armyDto = armyDto)

        val difference = amount - troopDto.amount
        if (specialists >= 4 && specialists + difference > 4) {
          Logger.warn(s"Cannot change amount of troop because it is a specialist: ${troopDto.specialist.get.name} and there are already: $specialists in army")
          Some(troopDto)
        } else {
          Some(troopDto.copy(amount = amount))
        }
      } else {
        Some(troopDto.copy(amount = amount))
      }
    })
  }

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

      val abilities = troop.abilities.map(ability => KTAbilityLogic.abilityDoToDto(ability))
        .toList
        .sortBy(_.name)

      val specialist = if (StringUtils.isBlank(specialistName)) {
        None
      } else {
        KTSpecialistLogic.getSpecialistForTroop(specialistName)
      }

      val newTroop = KTArmyTroopDto(uuid = UUID.randomUUID().toString,
        name = troopName,
        unit = troop.unit,
        faction = factionName,
        stats = troopStats,
        loadout = loadout,
        amount = 1,
        points = troop.points,
        level = 1,
        totalPoints = calculateTroopPoints(basePoints = troop.points, items = List(), loadout = loadout, level = 1),
        abilities = abilities,
        specialist = specialist,
        requiredUnits = troop.requiredUnits,
        maxInArmy = troopDo.get.maxInArmy)

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

            // check if the loadout is selectable it may not because it is a unique one and already used
            val selectable = loadout.unit.isEmpty || !armyDto.troops.exists(troop => {
              troop.unit == loadout.unit && troop.loadout.name == loadout.name
            })

            val selected = loadout.name == troopDto.loadout.name
            KTOptionLoadout(selected = selected, selectable = selectable, loadout = loadout)
          })


        val itemsForTroop = KTItemLogic.getPossibleItemsForTroop(troopDto)
          .map(item => {
            val selected = troopDto.items.exists(_.name == item.name)
            KTItemOptionDto(selected = selected, item = item)
          })


        val specialistsOption = KTSpecialistLogic.getSpecialistOptionForTroop(troopDto)

        Some(KTTroopOptionsDto(troop = troopDto,
          loadoutOptions = loadOutsForTroop,
          itemOptions = itemsForTroop,
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
    * Converts a [[KTLoadoutDo]] to its corresponding [[KTLoadoutDto]]
    *
    * @param loadoutDo the loadout to convert
    * @return
    */
  private def loadoutDoToDto(loadoutDo: KTLoadoutDo): KTLoadoutDto = {
    val weapons = KTWeaponLogic.weaponDosToSortedDtos(loadoutDo.weapons)
    val weaponPoints = weapons.map(_.points).sum

    val items = KTItemLogic.itemDosToSortedDtos(loadoutDo.items)
    val itemsPoints = items.map(_.points).sum

    KTLoadoutDto(name = loadoutDo.name,
      points = weaponPoints + itemsPoints,
      weapons = weapons,
      items = items,
      unit = loadoutDo.unit,
      maxPerUnit = loadoutDo.maxPerUnit)
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

    val checkedWithRequiredUnits = newTroops.filter(newTroop => {
      newTroop.requiredUnits.isEmpty || newTroops.exists(troop => newTroop.requiredUnits.contains(troop.unit))
    })

    val newFaction = if (checkedWithRequiredUnits.isEmpty) {
      Logger.info("No more troops in the army removing the faction")
      StringUtils.EMPTY
    } else {
      armyDto.faction
    }

    val newArmy = armyDto.copy(faction = newFaction, points = calculateArmyPoints(checkedWithRequiredUnits), troops = checkedWithRequiredUnits)
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
          .map(updatedTroop => updateArmyWithTroop(updatedTroop = updatedTroop.copy(totalPoints = calculateTroopPoints(basePoints = updatedTroop.points, items = updatedTroop.items, loadout = updatedTroop.loadout, level = updatedTroop.level)), armyDto = armyDto))
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
    armyDto.troops
      .filter(troop => troop.specialist.isDefined && troop.specialist.get.name == "Anführer")
      .sortBy(_.name)
  }

  /**
    * Gets all specialists from the army which are not a leader
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getSpecialistsFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops
      .filter(troop => troop.specialist.isDefined && troop.specialist.get.name != "Anführer")
      .sortBy(_.name)
  }

  /**
    * Gets all troops which are normal troops in the army
    *
    * @param armyDto the army containing the troops
    * @return
    */
  def getNormalTroopsFromArmy(armyDto: KTArmyDto): List[KTArmyTroopDto] = {
    armyDto.troops
      .filter(_.specialist.isEmpty)
      .sortBy(_.name)
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
  * @param uuid          the uuid of the troop
  * @param name          the name of the troop
  * @param unit          the unit the troop belongs to
  * @param faction       the faction of the troop
  * @param maxInArmy     how many of this troop may in the army
  * @param stats         the stats the troop has
  * @param loadout       the loadout the tropp is equiped with
  * @param amount        how many of the troop are in the army
  * @param points        the base points of the troop
  * @param level         the level of the troop
  * @param totalPoints   the total points the troop cost
  * @param abilities     the abilities the troop has
  * @param items         the items the troop is equiped with
  * @param specialist    when set the troop is a specialist
  * @param requiredUnits the units required for having this unit in the army
  */
case class KTArmyTroopDto(uuid: String,
                          name: String,
                          unit: String,
                          faction: String,
                          maxInArmy: Int,
                          stats: KTTroopStats,
                          loadout: KTLoadoutDto,
                          amount: Int,
                          points: Int,
                          level: Int,
                          totalPoints: Int,
                          abilities: List[KTAbilityDto],
                          items: List[KTItemDto] = List(),
                          specialist: Option[KTSpecialistTroopDto] = None,
                          requiredUnits: Set[String])

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
  * Option if an item is selected by the troop or not
  *
  * @param selected true when the item is selected false when not
  * @param item     the item itself
  */
case class KTItemOptionDto(selected: Boolean,
                           item: KTItemDto)


/**
  * Contains all options a troop can have
  *
  * @param troop             the troop te loadout is for
  * @param loadoutOptions    all possible loadouts options for the troop
  * @param items             all possible items the troop can equip
  * @param specialistsOption the specialist this troop has and which specials can or are be selected
  */
case class KTTroopOptionsDto(troop: KTArmyTroopDto,
                             loadoutOptions: List[KTOptionLoadout],
                             itemOptions: List[KTItemOptionDto],
                             specialistsOption: Option[KTSpecialistOptionDto])

/**
  * Loadout option
  *
  * @param selected   true when the loadout is currently selected in the troop
  * @param loadout    the loadout itself
  * @param selectable when false the loadout cannot be selected
  */
case class KTOptionLoadout(selected: Boolean,
                           selectable: Boolean,
                           loadout: KTLoadoutDto)

/**
  * Loadout a troop can get
  *
  * @param name       the name of the loadout
  * @param weapons    the weapons of the loadout
  * @param items      the items of the loadout
  * @param points     how many points is the loadout worth
  * @param maxPerUnit how often this loadout may be used per unit
  * @param unit       the name of the unit
  */
case class KTLoadoutDto(name: String,
                        points: Int,
                        weapons: List[KTWeaponDto],
                        items: List[KTItemDto],
                        maxPerUnit: Int,
                        unit: String)
