package models

import javax.persistence.{Entity, Id, ManyToOne, Table}
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
object ArmyTroopDAO {

  private val FINDER = new Model.Finder[Long, ArmyTroopDO](classOf[ArmyTroopDO])


  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[ArmyTroopDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }

  def addFromSoldierDto(soldierDto:SoldierDto, factionDo: ArmyFactionDO) : ArmyTroopDO = {

    // create all abilities for this troop
    soldierDto.abilities.foreach(AbilityDAO.addByAbilityDtos(_))

    val armyTroopDO = new ArmyTroopDO()
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
    armyTroopDO.save()
    armyTroopDO
  }

}

@Entity
@Table(name = "troop") class ArmyTroopDO extends Model {

  @Id val id: Long = 0L

  @NotNull var name: String = ""

  @NotNull var points:Int = 0

  @NotNull var modelType:String = ""

  @NotNull var speed:Int = 0

  @NotNull var sprint:Int = 0

  @NotNull var shoot:Int = 0

  @NotNull var fight:Int = 0

  @NotNull var survive:Int = 0

  @NotNull var size:Int = 0

  @NotNull var armour:Int = 0

  @NotNull var victoryPoints:Int = 0

  @ManyToOne var faction: ArmyFactionDO = null
}
