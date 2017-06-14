package models

import java.util
import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.CSVModels.CSVItemDto
import play.api.Logger

import scala.collection.JavaConversions._

/**
  * Created by tuxburner on 08.06.17.
  */
object ItemDAO {


  private val FINDER = new Model.Finder[Long, ItemDO](classOf[ItemDO])

  /**
    * Gets an item by it's name and faction.
    * @param name
    * @param factionDO
    * @return
    */
  def findByNameAndFaction(name: String, factionDO: FactionDO): ItemDO = {
    FINDER.where().ieq("name", name).and().eq("faction", factionDO).findUnique
  }

  def findByNameAndFactionName(name: String, faction: String): ItemDO = {
    FINDER.where().ieq("name", name).and().eq("faction.name", faction).findUnique
  }

  /**
    * Finds all items for the given faction ordered by its name
    * @param factionName
    * @return
    */
  def findAllItemsForFaction(factionName: String) : List[ItemDO] = {
    FINDER.where().ieq("faction.name",factionName).and().eq("noUpdate", false).orderBy().asc("name").findList.toList
  }

  /**
    * Adds an item from the csv to the given faction to the database.
    * @param csvItemDto
    * @param factionDo
    * @return
    */
  def addItemToFaction(csvItemDto: CSVItemDto, factionDo: FactionDO): ItemDO = {

    Logger.info("Creating item: " + csvItemDto.name + " for faction: " + factionDo.name)

    val newItemDo = new ItemDO()
    newItemDo.name = csvItemDto.name
    newItemDo.faction = factionDo
    newItemDo.points = csvItemDto.points
    newItemDo.rarity = csvItemDto.rarity
    newItemDo.noUpdate = csvItemDto.noUpgrade

    newItemDo.save()

    newItemDo
  }


  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[WeaponDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }
}

@Entity
@Table(name = "item")
class ItemDO extends Model {

  @Id
  val id: Long = 0L

  @NotNull
  var name: String = ""

  @NotNull
  var rarity: String = ""

  @NotNull
  @ManyToOne
  var faction: FactionDO = null


  @NotNull
  var points: Int = 0


  @NotNull
  var noUpdate: Boolean = false
}
