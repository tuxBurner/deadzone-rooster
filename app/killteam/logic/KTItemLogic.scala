package killteam.logic

import killteam.logic.KTArmyLogic.getTroopFromArmyByUUIDAndPerformChanges
import killteam.models.{KTItemDo, KTItemsDao, KTTroopDao}
import play.api.Logger

/**
  * Logic handling all the killteam item 
  */
object KTItemLogic {

  /**
    * Converts a [[KTItemDo]] to its corresponding [[KTItemDto]]
    *
    * @param itemDo the item to convert
    * @return
    */
  private def itemDoToDto(itemDo: KTItemDo): KTItemDto = {
    KTItemDto(name = itemDo.name,
      faction = itemDo.faction.name,
      points = itemDo.points)
  }

  /**
    * Transforms the given [[KTItemDo]]s to a [[List]] of [[KTItemDto]] and sorts them by there name
    *
    * @param itemDos the dos to convert
    * @return
    */
  def itemDosToSortedDtos(itemDos: Set[KTItemDo]): List[KTItemDto] = {
    itemDos
      .map(itemDoToDto(_))
      .toList
      .sortBy(_.name)
  }

  /**
    * Adds an item to the given troop
    *
    * @param itemName the name of the item to add
    * @param uuid     the uuid of the troop where to add the item
    * @param armyDto  the army containing the troop
    * @return
    */
  def setItemAtTroop(itemName: String, uuid: String, armyDto: KTArmyDto): KTArmyDto = {
    Logger.info(s"Setting item: $itemName at troop: $uuid")
    getTroopFromArmyByUUIDAndPerformChanges(uuid = uuid, armyDto = armyDto, troopDto => {
      KTItemsDao.getItemByNameAndFaction(itemName = itemName, factionName = troopDto.faction)
        .map(itemDo => {
          val itemDto = itemDoToDto(itemDo)
          val newTroopItems = (itemDto :: troopDto.items).sortBy(_.name)
          val troopWitNewItem = troopDto.copy(items = newTroopItems)
          Some(troopWitNewItem)
        })
        .getOrElse({
          Logger.error(s"Cannot find item: $itemName for faction: ${troopDto.faction}")
          None
        })
    })
  }

  /**
    * Removes an item to the given troop
    *
    * @param itemName the name of the item to remove
    * @param uuid     the uuid of the troop where to remove the item
    * @param armyDto  the army containing the troop
    * @return
    */
  def removeItemFromTroop(itemName: String, uuid: String, armyDto: KTArmyDto): KTArmyDto = {
    Logger.info(s"Removing item: $itemName from troop: $uuid")
    getTroopFromArmyByUUIDAndPerformChanges(uuid = uuid, armyDto = armyDto, troopDto => {
      val newTroopItems = troopDto.items.filterNot(_.name == itemName)
      val troopWitNewItem = troopDto.copy(items = newTroopItems)
      Some(troopWitNewItem)
    })
  }

  /**
    * Gets all possible items from the troop
    *
    * @param troopDto the troop to get the items for
    * @return
    */
  def getPossibleItemsForTroop(troopDto: KTArmyTroopDto): List[KTItemDto] = {
    KTTroopDao.getTroopByFactionAndName(troopName = troopDto.name, factionName = troopDto.faction)
      .map(troopDo => itemDosToSortedDtos(troopDo.items))
      .getOrElse({
        Logger.error(s"Troop: ${troopDto.name} not found in faction: ${troopDto.faction}")
        List()
      })
  }

  /**
    * Gets all items for the given faction
    *
    * @param factionName the name of the faction to get the items for
    * @return
    */
  def getAllItemsForFaction(factionName: String): List[KTItemDto] = {
    KTItemsDao.getItemsByFaction(factionName = factionName).map(itemDoToDto)
  }
}

/**
  * Represents an item
  *
  * @param name    the name of the item
  * @param faction the name of the faction the item belongs to
  * @param points  how many points id the item worth
  */
case class KTItemDto(name: String,
                     faction: String,
                     points: Int)
