package killteam.logic

import killteam.models.{KTWeaponDao, KTWeaponDo}

/**
  * Logic handling killteam weapons
  */
object KTWeaponLogic {
  /**
    * Gets all weapons by the faction
    *
    * @param factionName the name of the faction
    * @return
    */
  def getAllWeaponsByFactionName(factionName: String): List[KTWeaponDto] = {
    KTWeaponDao
      .getWeaponsByFactionName(fractionName = factionName)
      .map((weaponDoToDto))
  }

  /**
    * Transforms the given [[KTWeaponDo]] to a [[List]] of [[KTWeaponDto]] and sorts them by there name
    *
    * @param weaponDos the dos to convert
    * @return
    */
  def weaponDosToSortedDtos(weaponDos: List[KTWeaponDo]): List[KTWeaponDto] = {
    weaponDos
      .map(weaponDoToDto(_))
      .sortBy(_.name)
  }

  /**
    * Converts a [[KTWeaponDo]] to its corresponding [[KTWeaponDto]]
    *
    * @param weaponDo the weapon do to convert
    * @return
    */
  def weaponDoToDto(weaponDo: KTWeaponDo): KTWeaponDto = {
    KTWeaponDto(name = weaponDo.name,
      faction = weaponDo.faction.name,
      points = weaponDo.points,
      range = weaponDo.range,
      weaponType = weaponDo.weaponType,
      weaponTypeModifier = weaponDo.weaponTypeModifier,
      strength = weaponDo.strength,
      puncture = weaponDo.puncture,
      damage = weaponDo.damage,
      linkedWeapon = weaponDo.linkedWeapon)
  }
}

/**
  * Represents a weapon
  *
  * @param name               the name of the weapon
  * @param faction            the faction the weapon belongs to
  * @param points             how many points is the weapon worth
  * @param range              the range of the weapon
  * @param weaponType         the type of the weapon
  * @param weaponTypeModifier the modifier of the weapon type
  * @param strength           the strength of the weapon
  * @param puncture           the puncture of the weapon
  * @param damage             the damage of the weapon
  * @param linkedWeapon       when the weapon is linked weapon this is set
  */
case class KTWeaponDto(name: String,
                       faction: String,
                       points: Int,
                       range: Int,
                       weaponType: EWeaponType,
                       weaponTypeModifier: String,
                       strength: String,
                       puncture: Int,
                       damage: String,
                       linkedWeapon: String)
