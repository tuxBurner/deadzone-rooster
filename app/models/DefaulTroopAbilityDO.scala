package models

import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.Models.AbilityDto


object DefaulTroopAbilityDAO {

  def addAbilityForTroop(armyTroopDO: ArmyTroopDO, abilityDto: AbilityDto): Unit = {
    val abilityDO = AbilityDAO.addByAbilityDtos(abilityDto)
    if (abilityDO == null) {
      return
    }

    val defaulTroopAbilityDO = new DefaulTroopAbilityDO();
    defaulTroopAbilityDO.ability = abilityDO;
    defaulTroopAbilityDO.troop = armyTroopDO;
    defaulTroopAbilityDO.defaultValue = abilityDto.factor;

    defaulTroopAbilityDO.save()
  }
}

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 22:57
  */
@Entity
@Table(name = "def_troop_ability") class DefaulTroopAbilityDO extends Model {

  @Id val id: Long = 0L

  @ManyToOne var troop: ArmyTroopDO = null

  @ManyToOne var ability: AbilityDO = null

  @NotNull var defaultValue: Int = 0;
}
