package killteam.logic

import killteam.logic.KTArmyLogic.getTroopFromArmyByUUIDAndPerformChanges
import killteam.models.{KTLoadoutDao, KTLoadoutDo}
import play.api.Logger

object KTLoadoutLogic {

  /**
    * Gets all the loadout options for the troop
    *
    * @param troopDto the troop to get the loadout options for
    * @param armyDto  the army
    * @return
    */
  def getLoadoutOptionsForTroop(troopDto: KTArmyTroopDto, armyDto: KTArmyDto): List[KTOptionLoadout] = {
    getPossibleLoadoutsForTroop(troopDto)
      .map(loadout => {

        // check if the loadout is selectable it may not because it is a unique one and already used
        val selectableByUnit = loadout.unit.isEmpty || !armyDto.troops.exists(troop => {
          troop.unit == loadout.unit && troop.loadout.name == loadout.name
        })

        // check if this is a subloadout and if the maximum is already taken by the troops in the army
        val selectableSubLoadout = loadout.subLoadout.isEmpty || armyDto.troops.count(armyTroop => {
          armyTroop.name == troopDto.name && loadout.subLoadout == armyTroop.loadout.subLoadout && troopDto.uuid != armyTroop.uuid
        }) < loadout.subMax

        val selectable = selectableByUnit && selectableSubLoadout

        val selected = loadout.name == troopDto.loadout.name
        KTOptionLoadout(selected = selected, selectable = selectable, loadout = loadout)
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
    * Converts a [[KTLoadoutDo]] to its corresponding [[KTLoadoutDto]]
    *
    * @param loadoutDo the loadout to convert
    * @return
    */
  def loadoutDoToDto(loadoutDo: KTLoadoutDo): KTLoadoutDto = {
    val weapons = KTWeaponLogic.weaponDosToSortedDtos(loadoutDo.weapons)
    val weaponPoints = weapons.map(_.points).sum

    val items = KTItemLogic.itemDosToSortedDtos(loadoutDo.items)
    val itemsPoints = items.map(_.points).sum

    KTLoadoutDto(name = loadoutDo.name,
      points = weaponPoints + itemsPoints,
      weapons = weapons,
      items = items,
      unit = loadoutDo.unit,
      maxPerUnit = loadoutDo.maxPerUnit,
      subLoadout = loadoutDo.subLoadout,
      subMax = loadoutDo.subMax)
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
          val newLoadout = KTLoadoutLogic.loadoutDoToDto(loadoutDo)
          val troopDtoWithNewLoadout = troopDto.copy(loadout = newLoadout)
          Some(troopDtoWithNewLoadout)
        })
        .getOrElse({
          Logger.error(s"Cannot find loadout: $loadoutName for troop: ${troopDto.name} faction: ${troopDto.faction}")
          None
        })
    })
  }
}

/**
  * Loadout a troop can get
  *
  * @param name       the name of the loadout
  * @param weapons    the weapons of the loadout
  * @param items      the items of the loadout
  * @param points     how many points is the loadout worth
  * @param maxPerUnit how often this loadout may be used per unit
  * @param unit       the name of the unit
  * @param subLoadout when set this means this is a sub loadout which can be only n time equipped in the army
  * @param subMax     when subLoadout is set and this is > 0 means how often this can be equipped in the army
  */
case class KTLoadoutDto(name: String,
                        points: Int,
                        weapons: List[KTWeaponDto],
                        items: List[KTItemDto],
                        maxPerUnit: Int,
                        unit: String,
                        subLoadout: String,
                        subMax: Int)

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
