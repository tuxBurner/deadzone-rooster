package models

import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.Models.SoldierDto
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
    FINDER.where().eq("faction.name",factionName).findList.toList
  }

  def addFromSoldierDto(soldierDto: SoldierDto, factionDo: FactionDO): TroopDO = {

    Logger.info("Creating troop: " + soldierDto.name + " for faction: " + factionDo.name)

    val armyTroopDO = new TroopDO()
    armyTroopDO.faction = factionDo;
    armyTroopDO.name = soldierDto.name
    armyTroopDO.points = soldierDto.points
    armyTroopDO.modelType = soldierDto.soldierType.toString
    armyTroopDO.speed = soldierDto.speed._1
    armyTroopDO.sprint = soldierDto.speed._2
    armyTroopDO.shoot = soldierDto.shoot
    armyTroopDO.fight = soldierDto.fight
    armyTroopDO.survive = soldierDto.survive
    armyTroopDO.size = soldierDto.size
    armyTroopDO.armour = soldierDto.armour
    armyTroopDO.victoryPoints = soldierDto.victoryPoints

    // find the weapons
    soldierDto.defaultWeaponNames.foreach(weaponName => {
      val defaultWeapon = WeaponDAO.findByNameAndFaction(weaponName, factionDo)
      if (defaultWeapon == null) {
        Logger.error("Could not add default weapon " + weaponName + " to troop: " + armyTroopDO.name + " faction: " + factionDo.name + " was not found in the db")
      } else {
        armyTroopDO.defaultEquipment.addAll(defaultWeapon)
      }
    })

    armyTroopDO.save()

    soldierDto.abilities.foreach(DefaultTroopAbilityDAO.addAbilityForTroop(armyTroopDO, _))

    armyTroopDO
  }

}

@Entity
@Table(name = "troop") class TroopDO extends Model {

  @Id val id: Long = 0L

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

  @ManyToOne var faction: FactionDO = null

  @ManyToMany
  @JoinTable(name = "def_troop_weapon") var defaultEquipment: java.util.List[WeaponDO] = null

  @OneToMany
  var defaultTroopAbilities: java.util.List[DefaultTroopAbilityDO] = null
}
