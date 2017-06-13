package deadzone.models

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 15.02.17
  *         Time: 23:07
  */
object CSVModels {


  case class AbilityDto(title: String, factor: Int = 0)

  case class CSVWeaponBaseDto(faction: String, name: String, points: Int, victoryPoints: Int, range: Int, armorPircing: Int, weaponTypes:Array[String], hardPoint: Int = 0, free: Boolean = false, abilities: List[AbilityDto] = List())

  case class CSVItemDto(faction: String, name: String, points: Int, rarity: String, noUpgrade: Boolean)

  case class CSVSoldierDto(faction: String, name: String, points: Int, soldierType: ModelType.Value, speed: (Int, Int), shoot: Int, fight: Int, survive: Int, size: Int, armour: Int, victoryPoints: Int, abilities: List[AbilityDto], defaultWeaponNames: List[String], weaponTypes: Array[String], hardPoints:Int, recon: Int, armySpecial: String, defaultItems: List[String])


}


