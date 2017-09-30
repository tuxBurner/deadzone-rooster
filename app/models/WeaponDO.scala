package models

import deadzone.models.CSVModels.CSVWeaponDto
import play.api.Logger

import scala.collection.mutable.ListBuffer

object WeaponDAO {

  val weapons: ListBuffer[WeaponDO] = ListBuffer()

  def findByNameAndFactionAndAllowedTypes(name: String, factionDO: FactionDO, allowedTypes: List[String]): Option[WeaponDO] = {
    findByNameAndFactionNameAndAllowedTypes(name, factionDO.name, allowedTypes)
  }

  def findByNameAndFactionNameAndAllowedTypes(name: String, faction: String, allowedTypes: List[String]): Option[WeaponDO] = {
    findByFactionAndTypes(faction, allowedTypes)
      .find(_.name == name)
  }

  def findByFactionAndTypes(factionName: String, weaponTypes: List[String]): List[WeaponDO] = {
    weapons
      .filter(_.faction.name == factionName)
      .filter(_.weaponTypes.exists(weaponType => weaponTypes.contains(weaponType.name)))
      .toList
  }

  def addWeaponToFaction(csvWeaponDto: CSVWeaponDto, factionDo: FactionDO): WeaponDO = {

    Logger.info("Creating weapon: " + csvWeaponDto.name + " for faction: " + factionDo.name)


    val abilities = csvWeaponDto.abilities
      .map(csvAbility => DefaultWeaponAbilityDAO.addAbilityForWeapon(csvAbility))
      .filter(_.isDefined)
      .map(_.get)
      .toList


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

