package services.logic

import play.api.i18n.Messages

import scala.collection.mutable.ListBuffer

/**
  * Validates the army
  */
class ArmyValidator(messages: Messages) {


  val itemsPerPoint: Map[String, Map[Int, Int]] = Map("Common" -> Map(100 -> 2, 150 -> 3, 200 -> 4, 250 -> 5, 300 -> 6), "Rare" -> Map(100 -> 1, 150 -> 1, 200 -> 2, 250 -> 2, 300 -> 3), "Unique" -> Map(100 -> 1, 150 -> 1, 200 -> 1, 250 -> 1, 300 -> 1))


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
    *
    * @param army
    * @return
    */
  private def validateItems(army: ArmyDto): List[String] = {
    val result: ListBuffer[String] = ListBuffer()

    // check if there are troops with more than one item
    val troopsWithToMuchItems = army.troops.count(_.items.filter(_.noUpdate == false).length > 1)
    if (troopsWithToMuchItems > 0) result += messages("validate.troopsToMuchItems")


    // check if the rarity is okay for the items
    val itemsInArmy = army.troops.flatten(_.items.filter(_.noUpdate == false))


    result ++= validateItemsPerRarity(itemsInArmy, "Common", army.points)
    result ++= validateItemsPerRarity(itemsInArmy, "Unique", army.points)
    result ++= validateItemsPerRarity(itemsInArmy, "Rare", army.points)


    return result.toList
  }

  /**
    * Checks if the items in the army are matching the allowed amount of items of this rarity by army points
    *
    * @param itemsInArmy
    * @param rarity
    * @param armyTotalPoints
    * @return
    */
  private def validateItemsPerRarity(itemsInArmy: List[ArmyItemDto], rarity: String, armyTotalPoints: Int): List[String] = {

    val rarityItems = itemsInArmy.filter(_.rarity == rarity)
    // no items ?
    if (rarityItems.length == 0) {
      return List();
    }


    val armyItemsRange: Int =
      if (100 >= armyTotalPoints) {
        100
      } else if (150 >= armyTotalPoints) {
        150
      } else if (200 >= armyTotalPoints) {
        200
      } else if (250 >= armyTotalPoints) {
        250
      } else if (300 >= armyTotalPoints) {
        300
      } else {
        100
      }

    // get the allowed amount of items for the amount of army points
    val itemsPerArmyPoint = itemsPerPoint.get(rarity).get.get(armyItemsRange).get


    if (itemsPerArmyPoint < rarityItems.length) {
      List(messages("validate.toMuchItemsPerRarity", rarity, itemsPerArmyPoint))
    } else {
      List()
    }


  }

}
