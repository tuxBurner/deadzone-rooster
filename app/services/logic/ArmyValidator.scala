package services.logic

import deadzone.models.{ItemRarity, ModelType}
import play.api.i18n.Messages

import scala.collection.mutable.ListBuffer

/**
  * Validates the army
  */
class ArmyValidator(messages: Messages) {


  /**
    * How many items per rarity an army can have
    */
  val itemsPerPoint: Map[ItemRarity.Value, Map[Int, Int]] = Map(
    ItemRarity.Common -> Map(100 -> 2, 150 -> 3, 200 -> 4, 250 -> 5, 300 -> 6),
    ItemRarity.Rare -> Map(100 -> 1, 150 -> 1, 200 -> 2, 250 -> 2, 300 -> 3),
    ItemRarity.Unique -> Map(100 -> 1, 150 -> 1, 200 -> 1, 250 -> 1, 300 -> 1))


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
    getTroopAmountByType(army, ModelType.Leader) match {
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
    val troopCount = getTroopAmountByType(army, ModelType.Troop)
    val specialistCount = getTroopAmountByType(army, ModelType.Specialist)

    if (specialistCount > troopCount) {
      List(messages("validate.toMuchSpecialistSelected"))
    } else {
      List()
    }
  }

  /**
    * Checks if there are enough troops for the amount of vehicles
    *
    * @param army the army with the troops in it
    * @return
    */
  private def validateVehicle(army: ArmyDto): List[String] = {
    val troopCount = getTroopAmountByType(army, ModelType.Troop)
    val vehicleCount = getTroopAmountByType(army, ModelType.Vehicle)

    val allowedAmountOfVehicles: Int = troopCount / 3

    if (allowedAmountOfVehicles < vehicleCount) {
      List(messages("validate.toMuchVehiclesSelected"))
    } else {
      List()
    }
  }

  /**
    * Calculates the amount of troops of the given type in the army
    *
    * @param army      the army containing the troop
    * @param modelType the type of model to count
    * @return
    */
  private def getTroopAmountByType(army: ArmyDto, modelType: ModelType.Value): Int = {
    army.troopsWithAmount.filter(_.troop.modelType == modelType.toString).map(_.amount).sum
  }

  /**
    * Checks if there is only one characte in the army
    *
    * @param army
    * @return
    */
  private def validateCharacters(army: ArmyDto): List[String] = {
    getTroopAmountByType(army, ModelType.Character) match {
      case 0 => List()
      case 1 => List()
      case _ => List(messages("validate.onlyOneCharacterAllowed"))
    }
  }

  /**
    * Checks if the items the army contains are valid
    *
    * @param army the army with the troops in it
    * @return
    */
  private def validateItems(army: ArmyDto): List[String] = {
    val result: ListBuffer[String] = ListBuffer()

    // check if there are troops with more than one item

    val troopsWithToMuchItems = army.troopsWithAmount.count(_.troop.items.count(_.noUpdate == false) > 1)

    if (troopsWithToMuchItems > 0) result += messages("validate.troopsToMuchItems")


    // check if the rarity is okay for the items
    //val itemsInArmy = army.troopsWithAmount.flatten(_.troop.items.filter(_.noUpdate == false))


    result ++= validateItemsPerRarity(army, ItemRarity.Common)
    result ++= validateItemsPerRarity(army, ItemRarity.Unique)
    result ++= validateItemsPerRarity(army, ItemRarity.Rare)


    result.toList
  }

  /**
    * Checks if the items in the army are matching the allowed amount of items of this rarity by army points
    *
    * @param army   the army where the troops with there items are in
    * @param rarity the rarity to check
    * @return
    */
  private def validateItemsPerRarity(army: ArmyDto, rarity: ItemRarity.Value): List[String] = {

    val rarityItems = army.troopsWithAmount.map(amountTroop => {
      // count all items with the given rarity and no update
      val itemsInTroop = amountTroop.troop.items.count(item => item.rarity == rarity.toString && item.noUpdate == false)
      // multiply with the amount of troop
      itemsInTroop * amountTroop.amount
    }).sum

    // no items ?
    if (rarityItems == 0) {
      return List()
    }


    val armyItemsRange: Int =
      if (100 >= army.points) {
        100
      } else if (150 >= army.points) {
        150
      } else if (200 >= army.points) {
        200
      } else if (250 >= army.points) {
        250
      } else if (300 >= army.points) {
        300
      } else {
        100
      }

    // get the allowed amount of items for the amount of army points
    val itemsPerArmyPoint = itemsPerPoint.get(rarity).get.get(armyItemsRange).get


    if (itemsPerArmyPoint < rarityItems) {
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
