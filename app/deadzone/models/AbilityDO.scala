package deadzone.models

import deadzone.models.CSVModels
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.mutable.ListBuffer


/**
  * Handles all data access which is related to the [[AbilityDO]]
  */
object AbilityDAO {


  /**
    * The abilities which are available
    */
  val abilities: ListBuffer[AbilityDO] = ListBuffer()


  /**
    * Clears all the abilities
    */
  def clearAll(): Unit = {
    abilities.clear();
  }


  /**
    * Finds all abilities
    *
    * @return
    */
  def findAll(): List[AbilityDO] = {
    abilities.sortBy(_.name).toList
  }

  /**
    * Adds a new [[AbilityDO]] from a csv import, when it is not already in the inernal storage.
    *
    * @param csvAbilityDto the csv entry to import.
    * @return
    */
  def addByAbilityDtos(csvAbilityDto: CSVModels.CsvAbilityDto): Option[AbilityDO] = {
    findOrAddByName(csvAbilityDto.title, csvAbilityDto.factor != 0)
  }


  /**
    * Tries to find the ability by its name and if it does not exists it will be added internally.
    *
    * @param name     the name of the ability
    * @param incValue when true the ability factor can be incremented.
    * @return
    */
  def findOrAddByName(name: String, incValue: Boolean): Option[AbilityDO] = {

    if (StringUtils.isEmpty(name)) {
      return None
    }

    abilities
      .find(_.name == name)
      .orElse(
        {
          Logger.info("Creating ability: " + name + " in database")
          val newDbDo = AbilityDO(name, incValue)
          abilities += newDbDo
          Some(newDbDo)
        }
      )
  }
}

/**
  * The Do which represents an ability
  *
  * @param name      the name of the ability
  * @param hasIncVal when true this means the value of the ability can be incremented.
  */
case class AbilityDO(name: String,
                     hasIncVal: Boolean = false
                    )
