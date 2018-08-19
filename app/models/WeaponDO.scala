package models

import deadzone.models.CSVModels.CSVWeaponDto
import play.api.Logger

import scala.collection.mutable.ListBuffer

object WeaponDAO {

  val weapons: ListBuffer[WeaponDO] = ListBuffer()

  /**
    * Clears all weapons from this dao.
    */
  def clearAll(): Unit = {
    weapons.clear()
  }

  /**
    * Tries to find the weapon by it's name, belonging faction and allowed types
    *
    * @param name         the name of the weapon
    * @param factionDo    the faction the weapon belongs to
    * @param allowedTypes the type the weapon must have
    * @return
    */
  def findByNameAndFactionAndAllowedTypes(name: String, factionDo: FactionDO, allowedTypes: List[String]): Option[WeaponDO] = {
    findByNameAndFactionNameAndAllowedTypes(name, factionDo.name, allowedTypes)
  }

  /**
    * Tries to find the weapon by it's name, belonging faction and allowed types
    *
    * @param name         the name of the weapon
    * @param factionName  the name of the faction the weapon belongs to
    * @param allowedTypes the type the weapon must have
    * @return
    */
  def findByNameAndFactionNameAndAllowedTypes(name: String, factionName: String, allowedTypes: List[String]): Option[WeaponDO] = {
    findByFactionAndTypes(factionName, allowedTypes)
      .find(_.name == name)
  }

  /**
    * Returns all weapons as a [[List]] which belong to the given faction and is one of the weapon types.
    *
    * @param factionName the name of the faction
    * @param weaponTypes the weapontypes the weapons must have.
    * @return
    */
  def findByFactionAndTypes(factionName: String, weaponTypes: List[String]): List[WeaponDO] = {
    weapons
      .filter(_.faction.name == factionName)
      .filter(_.weaponTypes.exists(weaponType => weaponTypes.contains(weaponType.name)))
      .toList
  }

  /**
    * Finds all weapons by the given faction
    * @param factionName the name of the faction
    * @return
    */
  def findByFaction(factionName: String): List[WeaponDO] = {
    weapons
      .filter(_.faction.name == factionName)
      .toList
  }

  /**
    * Adds a weapon to a faction.
    *
    * @param csvWeaponDto the information about the weapon from the csv entry.
    * @param factionDo    the faction the weapon belongs to
    * @return
    */
  def addWeaponToFaction(csvWeaponDto: CSVWeaponDto, factionDo: FactionDO): WeaponDO = {

    Logger.info(s"Creating weapon: ${csvWeaponDto.name} / ${csvWeaponDto.range} for faction: ${factionDo.name}")


    val abilities = csvWeaponDto.abilities
      .map(csvAbility => DefaultWeaponAbilityDAO.createDefaultAbilityForWeapon(csvAbility))
      .filter(_.isDefined)
      .map(_.get)


    val weaponTypes = csvWeaponDto.weaponTypes.map(weaponType =>
      WeaponTypeDAO.findOrCreateTypeByName(weaponType)
    ).toList

    val newWeaponDo = WeaponDO(
      name = csvWeaponDto.name,
      points = csvWeaponDto.points,
      armorPircing = csvWeaponDto.armorPircing,
      faction = factionDo,
      free = csvWeaponDto.free,
      hartPoints = csvWeaponDto.hardPoint,
      shootRange = csvWeaponDto.range,
      victoryPoints = csvWeaponDto.victoryPoints,
      linkedName = csvWeaponDto.linkedName,
      weaponTypes = weaponTypes,
      defaultWeaponAbilities = abilities
    )


    weapons += newWeaponDo


    newWeaponDo
  }

}

/**
  * Class for the weapon
  *
  * @param name                   the name of the weapon
  * @param faction                the faction the weapon belongs to
  * @param weaponTypes            the types the weapon belongs to like Small Arms
  * @param victoryPoints          how many vps does the weapon have
  * @param points                 how many points does this weapon cost
  * @param shootRange             how far can this weapon shoot
  * @param armorPircing           the armor pircing of this weapon
  * @param hartPoints             how many hart points are used when this weapon is equipped
  * @param free                   is this weapon for free ?
  * @param linkedName             when set those weapons must be used together
  * @param defaultWeaponAbilities the abilities this weapon brings initially.
  */
case class WeaponDO(name: String,
                    faction: FactionDO,
                    weaponTypes: List[WeaponTypeDO] = List(),
                    victoryPoints: Int = 0,
                    points: Int = 0,
                    shootRange: Int = 0,
                    armorPircing: Int = 0,
                    hartPoints: Int = 0,
                    free: Boolean = false,
                    linkedName: String = "",
                    defaultWeaponAbilities: List[DefaultWeaponAbilityDO] = List()
                   )

