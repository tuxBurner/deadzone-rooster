package models

import java.util
import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.CSVModels.CSVWeaponBaseDto
import play.api.Logger

import scala.collection.JavaConversions._

/**
  * Created by tuxburner on 08.06.17.
  */
object WeaponDAO {


  private val FINDER = new Model.Finder[Long, WeaponDO](classOf[WeaponDO])

  def findByNameAndFaction(name: String, factionDO: FactionDO): util.List[WeaponDO] = {
    FINDER.where().ieq("name", name).and().eq("faction", factionDO).findList()
  }

  def findByNameAndFactionName(name: String, faction: String): WeaponDO = {
    FINDER.where().ieq("name", name).and().eq("faction.name", faction).findUnique
  }

  def findByFactionAndTypes(factionName: String, weaponTypes: java.util.List[String]): util.List[WeaponDO] = {
    FINDER.where().ieq("faction.name",factionName).and().in("weaponTypes.name",weaponTypes).findList
  }

  def addWeaponToFaction(weaponDto: CSVWeaponBaseDto, factionDo: FactionDO): WeaponDO = {

    Logger.info("Creating weapon: " + weaponDto.name + " for faction: " + factionDo.name)

    val newWeaponDo = new WeaponDO()
    newWeaponDo.name = weaponDto.name
    newWeaponDo.points = weaponDto.points
    newWeaponDo.armorPircing = weaponDto.armorPircing
    newWeaponDo.faction = factionDo
    newWeaponDo.free = weaponDto.free
    newWeaponDo.hartPoints = weaponDto.hardPoint
    newWeaponDo.shootRange = weaponDto.range
    newWeaponDo.victoryPoints = weaponDto.victoryPoints
    newWeaponDo.save()

    // add abilities to weapon
    weaponDto.abilities.foreach(ability => {
      DefaultWeaponAbilityDAO.addAbilityForWeapon(newWeaponDo, ability)
    })

    // add type to weapon
    weaponDto.weaponTypes.foreach(weaponType => {
      newWeaponDo.weaponTypes.add(WeaponTypeDAO.findOrCreateTypeByName(weaponType))
    })

    newWeaponDo.save()

    newWeaponDo
  }


  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[WeaponDO].getName + " from database")
    FINDER.all().toList.foreach(weaponDo => {
      weaponDo.weaponTypes.clear()
      weaponDo.update()
      weaponDo.delete()
    })
  }
}

@Entity
@Table(name = "weapon")
class WeaponDO extends Model {

  @Id
  val id: Long = 0L

  @NotNull
  var name: String = ""

  @NotNull
  @ManyToOne
  var faction: FactionDO = null

  @ManyToMany
  @NotNull
  var weaponTypes: java.util.List[WeaponTypeDO] = null


  @NotNull var victoryPoints: Int = 0

  @NotNull
  var points: Int = 0

  @NotNull
  var shootRange: Int = 0

  @NotNull
  var armorPircing: Int = 0

  @NotNull
  var hartPoints: Int = 0

  @NotNull
  var free: Boolean = false

  @OneToMany
  var defaultWeaponAbilities: java.util.List[DefaultWeaponAbilityDO] = null
}
