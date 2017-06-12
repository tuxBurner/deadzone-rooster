package services.logic

import models.{TroopDAO, TroopDO}
import scala.collection.JavaConversions._

/**
  * Created by tuxburner on 12.06.17.
  */
object ArmyLogic {

  def addTroopToArmy(factionName: String, troopName: String, army:ArmyDto): ArmyDto = {
    val troopDo = TroopDAO.findByFactionAndName(factionName,troopName)

     val newTroop = troopDoToArmyTroopDto(troopDo)

    val newTroops:List[ArmyTroopDto] = newTroop :: army.troops
    army.copy(troops = newTroops)
  }

  def troopDoToArmyTroopDto(troopDo:TroopDO): ArmyTroopDto = {

    val troopAbilities = troopDo.defaultTroopAbilities.toList.map(abilityDo => ArmyAbilityDto(abilityDo.ability.name,abilityDo.defaultValue))
    val weapons = troopDo.defaultEquipment.toList.map(_.name)

    ArmyTroopDto(
      troopDo.name,
      troopDo.points,
      troopDo.victoryPoints,
      troopDo.speed,
      troopDo.sprint,
      troopDo.armour,
      troopDo.size,
      troopDo.shoot,
      troopDo.fight,
      troopDo.survive,
      troopAbilities,
      weapons)
  }

}

case class ArmyDto (name: String, faction: String, troops: List[ArmyTroopDto] = List())

case class ArmyAbilityDto(name: String, defaultVal: Int)

case class ArmyTroopDto(name: String,
                        points: Int,
                        vp: Int,
                        speed: Int,
                        sprint: Int,
                        armour: Int,
                        size: Int,
                        shoot:Int,
                        fight:Int,
                        survive: Int,
                        abilities: List[ArmyAbilityDto],
                        weapons: List[String])
