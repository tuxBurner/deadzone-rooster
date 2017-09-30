package models

import deadzone.models.CSVModels
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.mutable.ListBuffer


object AbilityDAO {


  /**
    * The abilities which are avaible
    */
  val abilities: ListBuffer[AbilityDO] = ListBuffer()


  /**
    * Finds all abilities
    *
    * @return
    */
  def findAll(): List[AbilityDO] = {
    abilities.sortBy(_.name).toList
  }

  def addByAbilityDtos(abilityDto: CSVModels.AbilityDto): Option[AbilityDO] = {
    findOrAddByName(abilityDto.title, abilityDto.factor != 0)
  }


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
  * @param hasIncVal When true this means the value of the ability can be incremented.
  */
case class AbilityDO(name: String,
                     hasIncVal: Boolean = false
                    )
