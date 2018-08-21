package deadzone.models

import deadzone.models.CSVModels.CsvAbilityDto


/**
  * Handles the data access to the default abilities a [[TroopDO]] can have
  */
object DefaultTroopAbilityDAO {


  /**
    * Creates a new [[DefaultTroopAbilityDO]] from the given [[CsvAbilityDto]]
    *
    * @param abilityDto the ability to create the [[DefaultTroopAbilityDO]] from
    * @return
    */
  def createDefaultAbilityForTroop(abilityDto: CsvAbilityDto): Option[DefaultTroopAbilityDO] = {

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


/**
  * The default ability a [[TroopDO]] has from the beginning on.
  * @param ability the ability itself.
  * @param defaultValue the default value this ability has for the troop.
  */
case class DefaultTroopAbilityDO(ability: AbilityDO, defaultValue: Int = 0)
