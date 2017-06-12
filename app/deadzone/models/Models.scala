package deadzone.models

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 15.02.17
  *         Time: 23:07
  */
object Models {


  case class AbilityDto(title: String, factor: Int = 0)

  case class WeaponBaseDto(faction: String, name: String, points: Int, victoryPoints: Int, range: Int, armorPircing: Int, weaponType: String, hardPoint: Int = 0, free: Boolean = false, abilities: List[AbilityDto] = List())

  case class SoldierDto(faction: String, name: String, points: Int, soldierType: ModelType.Value, speed: (Int, Int), shoot: Int, fight: Int, survive: Int, size: Int, armour: Int, victoryPoints: Int, abilities: List[AbilityDto], defaultWeaponNames: List[String], hardPoints:Int)


}


