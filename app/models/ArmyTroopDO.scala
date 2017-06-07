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

  def addFromSoldierDro(soldierDto:SoldierDto, factionDo: ArmyFactionDO) : ArmyTroopDO = {
    val armyTroopDO = new ArmyTroopDO()
    armyTroopDO.faction = factionDo;
    armyTroopDO.name = soldierDto.name
    armyTroopDO.points = soldierDto.points
    armyTroopDO.modelType = soldierDto.soldierType.toString
    armyTroopDO.save()
    armyTroopDO
  }

}

@Entity
@Table(name = "troop") class ArmyTroopDO extends Model {

  @Id val id: Long = 0L

  @NotNull var name: String = ""

  @NotNull var points:Int = 0

  @NotNull var modelType:String = "";

  @ManyToOne var faction: ArmyFactionDO = null
}
