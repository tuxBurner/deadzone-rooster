package services.logic

import play.api.i18n.Messages

import scala.collection.mutable.ListBuffer

/**
  * Validates the army
  */
class ArmyValidator(messages: Messages) {


  /**
    * Validates the army if it is conform with the rules
    *
    * @param army
    */
  def validateArmy(army: ArmyDto): List[String] = {
    army.troops.length match {
      case 0 => List()
      case _ => checkSingleFaction(army) ++ checkIfOneLeader(army) ++ validateSpecialist(army) ++ validateVehicle(army) ++ validateCharacters(army) ++ validateItems(army)
    }
  }

  /**
    * Checks if the army is a single faction army
    *
    * @param army
    * @return
    */
  private def checkSingleFaction(army: ArmyDto): List[String] = {
    army.troops.map(_.faction).distinct.length match {
      case 1 => List()
      case _ => List(messages("validate.noSingleFaction"));
    }
  }

  /**
    * Validates if the army contains one leader
    *
    * @param army
    * @return
    */
  private def checkIfOneLeader(army: ArmyDto): List[String] = {
    army.troops.count(_.recon != 0) match {
      case 0 => List(messages("validate.noLeaderSelected"))
      case 1 => List()
      case _ => List(messages("validate.moreThanOneLeaderSelected"))
    }
  }

  /**
    * Checks if there are enough troops for the amount of specialists.
    *
    * @param army
    * @return
    */
  private def validateSpecialist(army: ArmyDto): List[String] = {
    val troopCount = army.troops.count(_.modelType == "Troop")
    val specialistCount = army.troops.count(_.modelType == "Specialist")

    if (specialistCount > troopCount) {
      List(messages("validate.toMuchSpecialistSelected"))
    } else {
      List()
    }
  }

  /**
    * Checks if there are enough troops for the amount of vehicles
    *
    * @param army
    * @return
    */
  private def validateVehicle(army: ArmyDto): List[String] = {
    val troopCount = army.troops.count(_.modelType == "Troop")
    val vehicleCount = army.troops.count(_.modelType == "Vehicle")

    val allowedAmountOfVehicles: Int = troopCount / 3;

    if (allowedAmountOfVehicles < vehicleCount) {
      List(messages("validate.toMuchVehiclesSelected"))
    } else {
      List()
    }
  }

  /**
    * Checks if there is only one characte in the army
    *
    * @param army
    * @return
    */
  private def validateCharacters(army: ArmyDto): List[String] = {
    army.troops.count(_.modelType == "Character") match {
      case 0 => List()
      case 1 => List()
      case _ => List(messages("validate.onlyOneCharacterAllowed"))
    }
  }

  /**
    * Checks if the items the army contains are valid
    * @param army
    * @return
    */
  private def validateItems(army: ArmyDto) : List[String] = {
    val result:ListBuffer[String] = ListBuffer()
    val troopsWithToMuchItems = army.troops.count(_.items.filter(_.noUpdate == false).length > 1)
    if(troopsWithToMuchItems > 0) result += messages("validate.troopsToMuchItems")

    return result.toList
  }

}
