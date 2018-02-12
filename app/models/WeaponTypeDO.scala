package models

import scala.collection.mutable.ListBuffer


/**
  * Class handling all the acces to the [[WeaponTypeDO]]
  */
object WeaponTypeDAO {


  /**
    * internal storage of the avaible [[WeaponTypeDO]]s in the application.
    */
  val weaponTypes: ListBuffer[WeaponTypeDO] = ListBuffer()


  /**
    * Checks if a [[WeaponTypeDO]] with the given name exists and if not creates it.
    * @param name the name of the [[WeaponTypeDO]]
    * @return
    */
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


/**
  * Type a weapon has
  * @param name the name of the type
  */
case class WeaponTypeDO(name: String)




