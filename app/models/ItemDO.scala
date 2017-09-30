package models

import deadzone.models.CSVModels.CSVItemDto
import deadzone.parsers.CSVWeaponImporter.NUMBER_REGEX
import play.api.Logger

import scala.collection.mutable.ListBuffer


object ItemDAO {

  val items: ListBuffer[ItemDO] = ListBuffer()


  /**
    * Gets an item by it's name and faction.
    *
    * @param name
    * @param factionDO
    * @return
    */
  def findByNameAndFaction(name: String, factionDO: FactionDO): Option[ItemDO] = {
    findByNameAndFactionName(name, factionDO.name)
  }

  def findByNameAndFactionName(name: String, faction: String): Option[ItemDO] = {
    items
      .find(item => (item.name == name && item.faction.name == faction))
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
    * Returns all items
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


case class ItemDO(name: String = "",
                  rarity: String = "",
                  faction: FactionDO = null,
                  points: Int = 0,
                  noUpdate: Boolean = false)

