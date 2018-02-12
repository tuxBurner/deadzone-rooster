package models

import deadzone.models.CSVModels.CsvAbilityDto


/**
  * Handles the default weapon abilities.
  */
object DefaultWeaponAbilityDAO {


  /**
    * Creates a new [[DefaultWeaponAbilityDO]] from the given [[CsvAbilityDto]]
    * @param abilityDto the abiltiy from the csv which is a default ability for a weapon.
    * @return
    */
  def createDefaultAbilityForWeapon(abilityDto: CsvAbilityDto): Option[DefaultWeaponAbilityDO] = {

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

/**
  * Class which holds the default ability of a weapon and the initial default value.
  * @param abilityDO
  * @param defaultValue
  */
case class DefaultWeaponAbilityDO(abilityDO: AbilityDO,defaultValue: Int = 0)



