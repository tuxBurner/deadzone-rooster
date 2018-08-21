package deadzone.logic

import models.WeaponDAO

import scala.collection.mutable.ListBuffer

object WeaponsLogic {

  /**
    * Gets all weapons of a faction grouped by there type
    * @param factionName the name of the faction to load
    * @return
    */
  def getAllWeaponsOfFaction(factionName: String): Map[String, List[ArmyWeaponDto]] = {


    val weaponDos = WeaponDAO.findByFaction(factionName)

    val weaponsByTypes: scala.collection.mutable.Map[String, ListBuffer[ArmyWeaponDto]] = scala.collection.mutable.Map()

    weaponDos.foreach(weapon => {

      val weaponDto = ArmyLogic.weaponDoToWeaponDto(weapon)

      weapon.weaponTypes.foreach(weaponType => {
        if(!weaponsByTypes.isDefinedAt(weaponType.name)) {
          weaponsByTypes.put(weaponType.name, ListBuffer())
        }

        weaponsByTypes(weaponType.name).append(weaponDto)
      })
    })



    weaponsByTypes
      .map(mapEntry => mapEntry._1 -> mapEntry._2.toList)
      .toMap
  }

}
