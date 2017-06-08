package models

import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.Models.AbilityDto
import play.api.Logger

import scala.collection.JavaConversions._


object DefaultTroopAbilityDAO {

  private val FINDER = new Model.Finder[Long, DefaultTroopAbilityDO](classOf[DefaultTroopAbilityDO])


  def addAbilityForTroop(armyTroopDO: TroopDO, abilityDto: AbilityDto): Unit = {
    val abilityDO = AbilityDAO.addByAbilityDtos(abilityDto)
    if (abilityDO == null) {
      return
    }

    val defaulTroopAbilityDO = new DefaultTroopAbilityDO();
    defaulTroopAbilityDO.ability = abilityDO;
    defaulTroopAbilityDO.troop = armyTroopDO;
    defaulTroopAbilityDO.defaultValue = abilityDto.factor;

    defaulTroopAbilityDO.save()
  }

  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[DefaultTroopAbilityDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }
}

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 22:57
  */
@Entity
@Table(name = "def_troop_ability") class DefaultTroopAbilityDO extends Model {

  @Id val id: Long = 0L

  @ManyToOne var troop: TroopDO = null

  @ManyToOne var ability: AbilityDO = null

  @NotNull var defaultValue: Int = 0
}
