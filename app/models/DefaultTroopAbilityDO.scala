package models

import deadzone.models.CSVModels.AbilityDto


object DefaultTroopAbilityDAO {


  def addAbilityForTroop(abilityDto: AbilityDto): Option[DefaultTroopAbilityDO] = {

    val abilityDO = AbilityDAO.addByAbilityDtos(abilityDto)

    if (abilityDO.isEmpty == true) {
      return None
    }

    Some(DefaultTroopAbilityDO(
      ability = abilityDO.get,
      defaultValue = abilityDto.factor
    ))

  }
}


case class DefaultTroopAbilityDO(ability: AbilityDO, defaultValue: Int = 0)
