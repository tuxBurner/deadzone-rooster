package models

import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.CSVModels.CSVSoldierDto
import play.api.Logger

import scala.collection.JavaConversions._

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 21:29
  */
object TroopDAO {

  private val FINDER = new Model.Finder[Long, TroopDO](classOf[TroopDO])


  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[TroopDO].getName + " from database")
    FINDER.all().toList.foreach(troopDo => {
      troopDo.defaultEquipment.clear()
      troopDo.update()
      troopDo.delete()
    })
  }

  def findAllForFactionByName(factionName: String): List[TroopDO] = {
    FINDER.where().eq("faction.name", factionName).findList.toList
  }

  def findByFactionAndName(factionName: String, name:String): TroopDO = {
    FINDER.where().eq("faction.name", factionName).and().eq("name",name).findUnique
  }

  def addFromSoldierDto(soldierDto: CSVSoldierDto, factionDo: FactionDO): TroopDO = {

    Logger.info("Creating troop: " + soldierDto.name + " for faction: " + factionDo.name)

    val troopDO = new TroopDO()
    troopDO.faction = factionDo
    troopDO.name = soldierDto.name
    troopDO.points = soldierDto.points
    troopDO.modelType = soldierDto.soldierType.toString
    troopDO.speed = soldierDto.speed._1
    troopDO.sprint = soldierDto.speed._2
    troopDO.shoot = soldierDto.shoot
    troopDO.fight = soldierDto.fight
    troopDO.survive = soldierDto.survive
    troopDO.size = soldierDto.size
    troopDO.armour = soldierDto.armour
    troopDO.victoryPoints = soldierDto.victoryPoints
    troopDO.hardPoints = soldierDto.hardPoints

    // find the weapons
    soldierDto.defaultWeaponNames.foreach(weaponName => {
      val defaultWeapon = WeaponDAO.findByNameAndFaction(weaponName, factionDo)
      if (defaultWeapon == null) {
        Logger.error("Could not add default weapon " + weaponName + " to troop: " + troopDO.name + " faction: " + factionDo.name + " was not found in the db")
      } else {
        troopDO.defaultEquipment.addAll(defaultWeapon)
      }
    })

    soldierDto.weaponTypes.foreach(weaponTypeName => {
      val weaponTypeDo = WeaponTypeDAO.findOrCreateTypeByName(weaponTypeName)
      troopDO.allowedWeaponTypes.add(weaponTypeDo);
    })

    troopDO.save()

    soldierDto.abilities.foreach(DefaultTroopAbilityDAO.addAbilityForTroop(troopDO, _))

    troopDO
  }

}

@Entity
@Table(name = "troop") class TroopDO extends Model {

  @Id val id: Long = 0L

  @Column(unique = true)
  @NotNull var name: String = ""

  @NotNull var points: Int = 0

  @NotNull var modelType: String = ""

  @NotNull var speed: Int = 0

  @NotNull var sprint: Int = 0

  @NotNull var shoot: Int = 0

  @NotNull var fight: Int = 0

  @NotNull var survive: Int = 0

  @NotNull var size: Int = 0

  @NotNull var armour: Int = 0

  @NotNull var victoryPoints: Int = 0

  @NotNull var hardPoints: Int = 0

  @ManyToOne var faction: FactionDO = null

  @ManyToMany
  @JoinTable(name = "def_troop_weapon") var defaultEquipment: java.util.List[WeaponDO] = null

  @OneToMany
  var defaultTroopAbilities: java.util.List[DefaultTroopAbilityDO] = null

  @ManyToMany
  var allowedWeaponTypes: java.util.List[WeaponTypeDO] = null
}
