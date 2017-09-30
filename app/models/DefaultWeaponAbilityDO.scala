package models

import deadzone.models.CSVModels.AbilityDto


object DefaultWeaponAbilityDAO {


  def addAbilityForWeapon(abilityDto: AbilityDto): Option[DefaultWeaponAbilityDO] = {

    val abilityDO = AbilityDAO.addByAbilityDtos(abilityDto)

    if (abilityDO.isEmpty == true) {
      return None
    }

    Some(DefaultWeaponAbilityDO(
      abilityDO.get,
      abilityDto.factor
    ))
  }
}

case class DefaultWeaponAbilityDO(
                                   abilityDO: AbilityDO,
                                   defaultValue: Int = 0
                                 )



