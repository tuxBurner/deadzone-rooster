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
    * @param army the army with the troops in it
    */
  def validateArmy(army: ArmyDto): List[String] = {
    army.troopsWithAmount.length match {
      case 0 => List()
      case _ => checkSingleFaction(army) ++ checkIfOneLeader(army) ++ validateSpecialist(army) ++ validateVehicle(army) ++ validateCharacters(army) ++ validateItems(army) ++ validateWeaponTypesPerTroop(army)
    }
  }

  /**
    * Checks if the army is a single faction army
    *
    * @param army the army with the troops in it
    * @return
    */
  private def checkSingleFaction(army: ArmyDto): List[String] = {
    army.troopsWithAmount.map(_.troop.faction).distinct.length match {
      case 1 => List()
      case _ => List(messages("validate.noSingleFaction"));
    }
  }

  /**
    * Validates if the army contains one leader
    *
    * @param army the army with the troops in it
    * @return
    */
  private def checkIfOneLeader(army: ArmyDto): List[String] = {
    army.troopsWithAmount.count(_.troop.recon != 0) match {
      case 0 => List(messages("validate.noLeaderSelected"))
      case 1 => List()
      case _ => List(messages("validate.moreThanOneLeaderSelected"))
    }
  }

  /**
    * Checks if there are enough troops for the amount of specialists.
    *
    * @param army the army with the troops in it
    * @return
    */
  private def validateSpecialist(army: ArmyDto): List[String] = {
    val troopCount = army.troopsWithAmount.count(_.troop.modelType == "Troop")
    val specialistCount = army.troopsWithAmount.count(_.troop.modelType == "Specialist")

    if (specialistCount > troopCount) {
      List(messages("validate.toMuchSpecialistSelected"))
    } else {
      List()
    }
  }

  /**
    * Checks if there are enough troops for the amount of vehicles
    *
    * @param army  the army with the troops in it
    * @return
    */
  private def validateVehicle(army: ArmyDto): List[String] = {
    val troopCount = army.troopsWithAmount.count(_.troop.modelType == "Troop")
    val vehicleCount = army.troopsWithAmount.count(_.troop.modelType == "Vehicle")

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
    army.troopsWithAmount.count(_.troop.modelType == "Character") match {
      case 0 => List()
      case 1 => List()
      case _ => List(messages("validate.onlyOneCharacterAllowed"))
    }
  }

  /**
    * Checks if the items the army contains are valid
    *
    * @param army  the army with the troops in it
    * @return
    */
  private def validateItems(army: ArmyDto): List[String] = {
    val result: ListBuffer[String] = ListBuffer()

    // check if there are troops with more than one item
    val troopsWithToMuchItems = army.troopsWithAmount.count(_.troop.items.filter(_.noUpdate == false).length > 1)
    if (troopsWithToMuchItems > 0) result += messages("validate.troopsToMuchItems")


    // check if the rarity is okay for the items
    val itemsInArmy = army.troopsWithAmount.flatten(_.troop.items.filter(_.noUpdate == false))


    result ++= validateItemsPerRarity(itemsInArmy, "Common", army.points)
    result ++= validateItemsPerRarity(itemsInArmy, "Unique", army.points)
    result ++= validateItemsPerRarity(itemsInArmy, "Rare", army.points)


    result.toList
  }

  /**
    * Checks if the items in the army are matching the allowed amount of items of this rarity by army points
    *
    * @param itemsInArmy all items in the army
    * @param rarity the rarity to check
    * @param armyTotalPoints the total amount of points in this army
    * @return
    */
  private def validateItemsPerRarity(itemsInArmy: List[ArmyItemDto], rarity: String, armyTotalPoints: Int): List[String] = {

    val rarityItems = itemsInArmy.filter(_.rarity == rarity)
    // no items ?
    if (rarityItems.length == 0) {
      return List()
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

  /**
    * Checks if the weapon types ranged, fight are single
    *
    * @param army the army with the troops in it
    * @return
    */
  private def validateWeaponTypesPerTroop(army: ArmyDto): List[String] = {

    val result: ListBuffer[String] = ListBuffer()

    army.troopsWithAmount.foreach(amountTroop => {
      val fightWeapons = amountTroop.troop.weapons.filter(weapon => weapon.shootRange == 0 && weapon.free == false)

      val mappedDefaultWeapons = amountTroop.troop.defaultWeapons.count(defaultWeapon => amountTroop.troop.weapons.find(_.name == defaultWeapon.name).isDefined)

      if (mappedDefaultWeapons != amountTroop.troop.weapons.length) {
        if (fightWeapons.length > 1) {
          result += messages("validate.toMuchFightWeapons", amountTroop.troop.name, fightWeapons.length)
        }

        val shootWeapons = amountTroop.troop.weapons.filter(weapon => weapon.shootRange != 0 && weapon.free == false)
        if (shootWeapons.length > 1) {
          result += messages("validate.toMuchShootWeapons", amountTroop.troop.name, shootWeapons.length)
        }
      }
    })

    result.toList

  }

}
