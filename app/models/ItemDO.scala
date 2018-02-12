package models

import deadzone.models.CSVModels.CSVItemDto
import deadzone.parsers.CSVWeaponImporter.NUMBER_REGEX
import play.api.Logger

import scala.collection.mutable.ListBuffer


/**
  * Handles all the access to the [[ItemDO]]s
  */
object ItemDAO {

  /**
    * Internal storage if the [[ItemDO]]s
    */
  val items: ListBuffer[ItemDO] = ListBuffer()


  /**
    * Clears all items
    */
  def clearAll(): Unit = {
    items.clear()
  }


  /**
    * Gets an item by it's name and faction.
    *
    * @param name the name of the item
    * @param factionDO the faction the item belongs to
    * @return
    */
  def findByNameAndFaction(name: String, factionDO: FactionDO): Option[ItemDO] = {
    findByNameAndFactionName(name, factionDO.name)
  }

  /**
    * Find an item by it's name and factionName
    * @param name the name of the item
    * @param factionName the name of the faction
    * @return
    */
  def findByNameAndFactionName(name: String, factionName: String): Option[ItemDO] = {
    items
      .find(item => item.name == name && item.faction.name == factionName)
  }

  /**
    * Finds all items for the given faction ordered by its name
    *
    * @param factionName
    * @return
    */
  def findAllItemsForFaction(factionName: String): List[ItemDO] = {
    items
      .filter(item => item.faction.name == factionName && item.noUpdate == false)
      .sortBy(_.name)
      .toList
  }

  /**
    * Returns all items sorted by there name
    *
    * @return
    */
  def findAllItems(): List[ItemDO] = {
    items
      .groupBy(_.name)
      .map(_._2.head)
      .toList
      .filter(_.name.startsWith("M|") == false)
      .map(item => {
        item.copy(name = NUMBER_REGEX.replaceAllIn(item.name, ""))
      })
      .sortWith(_.name < _.name)
  }

  /**
    * Adds an item from the csv to the given faction to the database.
    *
    * @param csvItemDto
    * @param factionDo
    * @return
    */
  def addItemToFaction(csvItemDto: CSVItemDto, factionDo: FactionDO): ItemDO = {

    Logger.info("Creating item: " + csvItemDto.name + " for faction: " + factionDo.name)

    val newItemDo = new ItemDO(
      name = csvItemDto.name,
      faction = factionDo,
      points = csvItemDto.points,
      rarity = csvItemDto.rarity,
      noUpdate = csvItemDto.noUpgrade
    )

    items += newItemDo

    newItemDo
  }

}

/**
  * The class describing the item
  * @param name the name of the item
  * @param rarity the ratity of this item (rare/common/unique)
  * @param faction the faction the item belongs to
  * @param points the amount of points this item costs
  * @param noUpdate when true this item cannot be added to a troop it is only as default item avaible.
  */
case class ItemDO(name: String = "",
                  rarity: String = "",
                  faction: FactionDO = null,
                  points: Int = 0,
                  noUpdate: Boolean = false)

