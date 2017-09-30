package models

import deadzone.models.CSVModels
import org.apache.commons.lang3.StringUtils
import play.api.Logger

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 22:50
  */
object AbilityDAO {

  /**
    * Finds all abilities
    * @return
    */
  def findAll(): List[AbilityDO] = {
    FINDER.order().asc("name").findList().toList
  }

  def addByAbilityDtos(abilityDto: CSVModels.AbilityDto): AbilityDO = {
    findOrAddByName(abilityDto.title, abilityDto.factor != 0)
  }


  def findOrAddByName(name: String, incValue: Boolean): AbilityDO = {

    if (StringUtils.isEmpty(name)) {
      return null;
    }

    val dbDo = FINDER.where().ieq("name", name).findUnique()
    if (dbDo != null) {
      return dbDo
    }

    Logger.info("Creating ability: " + name + " in database")
    val newDbDo = new AbilityDO()
    newDbDo.name = name
    newDbDo.hasIncVal = incValue
    newDbDo.save()
    newDbDo
  }
}

/**
  * The Do which represents an ability
  * @param name the name of the ability
  * @param hasIncVal When true this means the value of the ability can be incremented.
  */
case class AbilityDO( name: String,
                      hasIncVal: Boolean = false
                    );
