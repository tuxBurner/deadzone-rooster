package deadzone.models

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 15.02.17
  *         Time: 23:07
  */
object CSVModels {


  case class CsvAbilityDto(title: String, factor: Int = 0)

  case class CSVWeaponDto(faction: String,
                          name: String,
                          points: Int,
                          victoryPoints: Int,
                          range: Int,
                          armorPircing: Int,
                          weaponTypes:Array[String],
                          hardPoint: Int = 0,
                          free: Boolean = false,
                          abilities: List[CsvAbilityDto] = List(),
                          linkedName: String = "")

  case class CSVItemDto(faction: String,
                        name: String,
                        points: Int,
                        rarity: String,
                        noUpgrade: Boolean)

  case class CSVTroopDto(faction: String,
                         name: String,
                         points: Int,
                         soldierType: ModelType.Value,
                         speed: (Int, Int),
                         shoot: Int,
                         fight: Int,
                         survive: Int,
                         size: Int,
                         armour: Int,
                         victoryPoints: Int,
                         abilities: List[CsvAbilityDto],
                         defaultWeaponNames: List[String],
                         weaponTypes: Array[String],
                         hardPoints:Int,
                         recon: Int,
                         armySpecial: String,
                         defaultItems: List[String],
                         imageUrl: String)


}


