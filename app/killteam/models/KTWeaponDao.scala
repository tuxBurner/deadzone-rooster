package killteam.models

import killteam.parsers.KTCsvWeaponDto
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Handles all data access to the weapons database
  */
object KTWeaponDao {
  /**
    * All killteam weapons
    */
  val weapons: ListBuffer[KTWeaponDo] = ListBuffer()

  /**
    * Adds a weapon to the given faction
    *
    * @param csvWeaponDto the weapon information's from the csv
    * @param factionDo    the faction the weapon belongs to
    * @return
    */
  def addWeaponToFaction(csvWeaponDto: KTCsvWeaponDto, factionDo: KTFactionDo): KTWeaponDo = {
    Logger.info(s"KT adding weapon: ${csvWeaponDto.name} to faction: ${factionDo.name}")

    val weaponDo = KTWeaponDo(name = csvWeaponDto.name,
      faction = factionDo,
      points = csvWeaponDto.points,
      range = csvWeaponDto.range,
      weaponType = csvWeaponDto.weaponType,
      strength = csvWeaponDto.strength,
      puncture = csvWeaponDto.puncture,
      damage = csvWeaponDto.damage,
      linkedWeapon = csvWeaponDto.linkedWeapon)

    weapons += weaponDo

    weaponDo
  }

  /**
    * Gets the weapon by its name and faction
    *
    * @param name      the name of the weapon
    * @param factionDo the faction for the weapon
    * @return
    */
  def getWeaponByNameAndFaction(name: String, factionDo: KTFactionDo): Option[KTWeaponDo] = {
    weapons.find(weaponDo => weaponDo.name == name && weaponDo.faction.name == factionDo.name)
  }

  /**
    * Gets all weapons by there linked name
    *
    * @param linkedName the linkedName of the weapon
    * @param factionDo  the faction of the weapon
    * @return
    */
  def getWeaponsByLinkedNameAndFaction(linkedName: String, factionDo: KTFactionDo): Set[KTWeaponDo] = {
    weapons.filter(weaponDo => weaponDo.linkedWeapon == linkedName && weaponDo.faction.name == factionDo.name).toSet
  }
}

/**
  * Represents a weapon
  *
  * @param name         the name of the weapon
  * @param faction      the faction the weapon belongs to
  * @param points       how many points the weapon is worth
  * @param range        th range of the weapon
  * @param weaponType   the type of the weapon
  * @param strength     the strength of the weapon
  * @param puncture     the puncture damage of the weapon
  * @param damage       the damage the weapon causes
  * @param linkedWeapon when set this is a linked weapon
  */
case class KTWeaponDo(name: String,
                      faction: KTFactionDo,
                      points: Int,
                      range: Int,
                      weaponType: String,
                      strength: String,
                      puncture: Int,
                      damage: String,
                      linkedWeapon: String)

