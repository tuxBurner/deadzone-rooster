package models

import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.Models.WeaponBaseDto
import play.api.Logger

import scala.collection.JavaConversions._

/**
  * Created by tuxburner on 08.06.17.
  */
object WeaponDAO {


  private val FINDER = new Model.Finder[Long, WeaponDO](classOf[WeaponDO])

  def addWeaponToFaction(weaponDto: WeaponBaseDto, factionDo: ArmyFactionDO): Unit = {

    val newWeaponDo = new WeaponDO()
    newWeaponDo.name = weaponDto.name
    newWeaponDo.points = weaponDto.points
    newWeaponDo.armorPircing = weaponDto.armorPircing
    newWeaponDo.faction = factionDo
    newWeaponDo.free = weaponDto.free
    newWeaponDo.weaponType = weaponDto.weaponType
    newWeaponDo.weaponSubType = weaponDto.subWeaponType
    newWeaponDo.hartPoints = weaponDto.hardPoint
    newWeaponDo.shootRange = weaponDto.range
    newWeaponDo.victoryPoints = weaponDto.victoryPoints
    newWeaponDo.save()
  }


  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[WeaponDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
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
  var faction:ArmyFactionDO = null


  var weaponType: String = null

  var weaponSubType:String = null

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


}