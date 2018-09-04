package killteam.models

import killteam.parsers.KTCsvLoadoutDto
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Handles all data access to the weapons database
  */
object KTLoadoutDao {

  /**
    * All killteam loadouts
    */
  val loadOuts: ListBuffer[KTLoadoutDo] = ListBuffer()


  /**
    * Gets the default loadout for the troop in the faction
    *
    * @param troopName   the name of the troop
    * @param factionName the name of the faction
    * @return
    */
  def getDefaultLoadout(troopName: String, factionName: String): Option[KTLoadoutDo] = {
    getLoadoutByTroopAndName(troopName, factionName, "Basis")
  }


  /**
    * Get the loadout by the given toop/faction and name
    *
    * @param troopName   the name of the troop
    * @param factionName the name of the faction
    * @param loadoutName the name of the loadout
    * @return
    */
  def getLoadoutByTroopAndName(troopName: String, factionName: String, loadoutName: String): Option[KTLoadoutDo] = {
    loadOuts.find(loadout => loadout.troop.name == troopName && loadout.faction.name == factionName && loadout.name == loadoutName)
  }

  /**
    * Gets all loadouts fot the given troop and faction
    *
    * @param troopName   the name of the troop
    * @param factionName the name of the faction
    * @return
    */
  def getLoadoutsByTroopAndName(troopName: String, factionName: String): List[KTLoadoutDo] = {
    loadOuts.filter(loadOut => loadOut.faction.name == factionName && loadOut.troop.name == troopName).toList
  }


  /**
    * Adds a weapon to the given faction
    *
    * @param csvLoadoutDto the loadout information's from the csv
    * @param factionDo     the faction the loadout belongs to
    * @param troopDo       the troop the loadout belongs to
    * @return
    */
  def addLoadoutToFactionAndTroop(csvLoadoutDto: KTCsvLoadoutDto, factionDo: KTFactionDo, troopDo: KTTroopDo): KTLoadoutDo = {

    Logger.info(s"KT adding Loadout: ${csvLoadoutDto.name} to troop: ${troopDo.name} and faction: ${factionDo.name}")

    val weapons: ListBuffer[KTWeaponDo] = ListBuffer()
    csvLoadoutDto.weapons.foreach(weaponName => {
      val weaponDo = KTWeaponDao.getWeaponByNameAndFaction(weaponName, factionDo)
      if (weaponDo.isEmpty) {

        //  check if the name is a linked weapon name
        val linkedWeapons = KTWeaponDao.getWeaponsByLinkedNameAndFaction(linkedName = weaponName, factionDo)
        if (linkedWeapons.isEmpty == false) {
          Logger.info(s"Found weapons by linkedname: $weaponName for faction: ${factionDo.name} for loadout: ${csvLoadoutDto.name}")
          weapons ++= linkedWeapons
        } else {
          Logger.error(s"KT cannot find weapon: $weaponName for faction: ${factionDo.name} in loadout: ${csvLoadoutDto.name}")
        }
      } else {
        weapons += weaponDo.get
      }
    })

    val items: ListBuffer[KTItemDo] = ListBuffer()
    csvLoadoutDto.items.foreach(itemName => {
      val itemDo = KTItemsDao.getItemByNameAndFaction(itemName, factionDo)
      if (itemDo.isEmpty) {
        Logger.error(s"KT cannot find item: $itemName for faction: ${factionDo.name} in loadout: ${csvLoadoutDto.name}")
      } else {
        items += itemDo.get
      }
    })

    val loadout = KTLoadoutDo(name = csvLoadoutDto.name,
      troop = troopDo,
      faction = factionDo,
      weapons = weapons.toList,
      items = items.toSet,
      maxPerUnit = csvLoadoutDto.maxPerUnit,
      unit = csvLoadoutDto.unit
    )

    loadOuts += loadout

    loadout
  }
}


/**
  * Represents a loadout
  *
  * @param faction    the faction the loadout belongs to
  * @param troop      the troop of the loadout
  * @param name       the name of the loadout
  * @param weapons    the weapons in the loadout
  * @param items      the items in the loadout
  * @param maxPerUnit how often may this loadout be equiped on a unit
  * @param unit       when max in army != 0 and this is set this means only n unit may equip this
  */
case class KTLoadoutDo(faction: KTFactionDo,
                       troop: KTTroopDo,
                       name: String,
                       weapons: List[KTWeaponDo],
                       items: Set[KTItemDo],
                       maxPerUnit: Int,
                       unit: String)



