package services.logic

/**
  * Does the im exp of an army
  */
object ArmyImExpLogic {


  /**
    * Exports a list of troops in an army
    *
    * @param army
    * @return
    */
  def troopsToExportTroops(army: ArmyDto): List[TroopExportDto] = {
    army.troops.map(troop => TroopExportDto(troop.faction, troop.name, troop.weapons.map(_.name), troop.items.map(_.name)))
  }

  case class TroopExportDto(faction: String, name: String, weapons: List[String], items: List[String])

}
