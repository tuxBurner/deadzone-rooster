package models.killteam

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
      weapons = weapons.toSet,
      items = items.toSet
    )

    loadOuts += loadout

    loadout
  }
}


/**
  * Represents a loadout
  *
  * @param faction the faction the loadout belongs to
  * @param troop   the troop of the loadout
  * @param name    the name of the loadout
  * @param weapons the weapons in the loadout
  * @param items   the items in the loadout
  */
case class KTLoadoutDo(faction: KTFactionDo,
                       troop: KTTroopDo,
                       name: String,
                       weapons: Set[KTWeaponDo],
                       items: Set[KTItemDo])



