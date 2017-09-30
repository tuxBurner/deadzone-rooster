package models

import scala.collection.mutable.ListBuffer


object WeaponTypeDAO {


  val weaponTypes: ListBuffer[WeaponTypeDO] = ListBuffer()


  def findOrCreateTypeByName(name: String): WeaponTypeDO = {
    weaponTypes
      .find(_.name == name)
      .getOrElse({
        val newTypeDo = new WeaponTypeDO(name)
        weaponTypes += newTypeDo
        newTypeDo
      })
  }

}


case class WeaponTypeDO(name: String)




