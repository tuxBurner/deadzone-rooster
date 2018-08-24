package killteam.logic

import killteam.models.{KTSpecialistDao, KTSpecialistDo, KTTroopDao}
import org.apache.commons.lang3.StringUtils
import play.api.Logger


/**
  * Logic handling specialist data
  */
object KTSpecialistLogic {


  /**
    * Gets the possible specialist options for the given troop type and army
    *
    * @param troopName   the name of the troop to get the specials for
    * @param factionName the name of the faction
    * @param armyDto     the army where to check for which specials are avaible
    * @return
    */
  def getAvaibleSpecialistSelectOptions(troopName: String, factionName: String, armyDto: KTArmyDto): List[String] = {
    val specialistsForTroop = KTTroopDao.getTroopByFactionAndName(troopName = troopName, factionName = factionName)
      .map(_.specialists.map(_.name).toList)
      .getOrElse(List("None"))

    val returnList = "" :: specialistsForTroop

    if(armyDto.troops.isEmpty) {
      returnList
    } else {
      returnList.filterNot(specialistName => armyDto.troops.exists(troop => troop.specialist.isDefined && troop.specialist.get.name == specialistName))
    }
  }

  /**
    * Gets the specialist option by the name of the specialist
    *
    * @param name the name of the specialist
    * @return
    */
  def getSpecialistOptionByName(name: String): Option[KTSpecialistOptionDto] = {
    KTSpecialistDao.findSpecialistByName(name)
      .map(specialistDo => {
        val specialTree = Some(findSpecialByLevel(specialistDo, 1, StringUtils.EMPTY).head)
        Some(KTSpecialistOptionDto(name = specialistDo.name,  baseSpecial = specialTree))
      })
      .getOrElse({
        Logger.error(s"Cannot find specialist: $name")
        None
      })
  }

  /**
    * Gets the possible specialist option for the troop when set
    *
    * @param troopDto the troop for which to get the specialist
    * @return
    */
  def getSpecialistOptionForTroop(troopDto: KTArmyTroopDto): Option[KTSpecialistOptionDto] = {

    if(troopDto.specialist.isEmpty) {
      return None
    }

    getSpecialistOptionByName(troopDto.specialist.get.name)
  }

  /**
    * Gets the specialist for setting it at the troop
    * @param specialistName the name of the specialist to set
    * @return
    */
  def getSpecialistForTroop(specialistName: String) : Option[KTSpecialistTroopDto] = {
    KTSpecialistDao.findSpecialistByName(specialistName)
      .map(specialistDo => {

        val baseSpecial = specialistDo.specials.find(_.level == 1)
          .map(specialDo => {
            List(KTSpecialTroopDto(name = specialDo.name, level = specialDo.level))
          })
          .getOrElse(List())

        Some(KTSpecialistTroopDto(name = specialistName, selectedSpecials = baseSpecial))
      })
      .getOrElse({
        Logger.error(s"Cannot find specialist: $specialistName")
        None
      })
  }


  /**
    * Goes through the specials of the specialists and traverse them level by level
    *
    * @param specialistDo       the specialist to traverse the specials from
    * @param level              the level to get the specials for
    * @param requireSpecialName the parent special
    * @return
    */
  private def findSpecialByLevel(specialistDo: KTSpecialistDo, level: Int, requireSpecialName: String): List[KTSpecialOptionDto] = {
    specialistDo.specials.filter(specialDo => specialDo.level == level && specialDo.require == requireSpecialName)
      .map(specialDo => {
        // find sub specials for the special
        val subSpecials = if (level == 3) {
          List()
        } else {
          findSpecialByLevel(specialistDo, level + 1, specialDo.name)
        }

        val selected = level == 1
        val selectable = level != 1

        KTSpecialOptionDto(selectable = selectable,
          selected = selected,
          level = level,
          name = specialDo.name,
          subSpecials = subSpecials)
      })
      .toList
  }

}


case class KTSpecialistOptionDto(name: String,
                                 baseSpecial: Option[KTSpecialOptionDto])

case class KTSpecialOptionDto(selectable: Boolean,
                              selected: Boolean,
                              name: String,
                              level: Int,
                              subSpecials: List[KTSpecialOptionDto] = List())

/**
  * Represents a specialist of a troop
  *
  * @param name     the name of the specialist
  * @param selectedSpecials the specials the specialist currently has
  */
case class KTSpecialistTroopDto(name: String,
                                selectedSpecials: List[KTSpecialTroopDto])

/**
  * Represents a special a troop can have
  *
  * @param name  the name of the special
  * @param level the level when this special was aquired
  */
case class KTSpecialTroopDto(name: String,
                             level: Int)
