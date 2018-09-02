package killteam.models

import killteam.parsers.KTCsvItemDto
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Handles all data access to the items database
  */
object KTItemsDao {
  /**
    * All killteam items
    */
  val items: ListBuffer[KTItemDo] = ListBuffer()



  /**
    * Gets the item by its name and faction
    *
    * @param itemName  the name  of the item
    * @param factionDo the faction the item belongs to
    * @return
    */
  def getItemByNameAndFaction(itemName: String, factionDo: KTFactionDo): Option[KTItemDo] = {
    getItemByNameAndFaction(itemName = itemName, factionName = factionDo.name)
  }

  /**
    * Gets the item by its name and faction name
    *
    * @param itemName    the name  of the item
    * @param factionName the name of the faction the item belongs to
    * @return
    */
  def getItemByNameAndFaction(itemName: String, factionName: String): Option[KTItemDo] = {
    items.find(itemDo => itemDo.faction.name == factionName && itemDo.name == itemName)
  }


  /**
    * Adds an item to the given faction
    *
    * @param csvItemDtothe item information's from the csv
    * @param factionDo     the faction the weapon belongs to
    * @return
    */
  def addItemToFaction(csvItemDto: KTCsvItemDto, factionDo: KTFactionDo): KTItemDo = {

    Logger.info(s"KT adding item: ${csvItemDto.name} to faction: ${factionDo.name}")

    val itemDo = KTItemDo(name = csvItemDto.name,
      faction = factionDo,
      points = csvItemDto.points,
    )

    items += itemDo

    itemDo
  }

  /**
    * Gets all items for the given faction
    * @param factionName the name of the faction
    * @return
    */
  def getItemsByFaction(factionName: String) : List[KTItemDo] = {
    items.filter(_.faction.name == factionName).sortBy(_.name).toList
  }
}

/**
  * Represents an item
  *
  * @param faction the faction the item belongs to
  * @param name    the name of the item
  * @param points  how many points is the item worth
  */
case class KTItemDo(faction: KTFactionDo,
                    name: String,
                    points: Int)

