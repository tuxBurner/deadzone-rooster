package killteam.logic

import killteam.models.{KTSpecialistDao, KTSpecialistDo, KTTroopDao}
import org.apache.commons.lang3.StringUtils
import play.api.Logger


/**
  * Logic handling specialist data
  */
object KTSpecialistLogic {

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
        Some(KTSpecialistOptionDto(name = specialistDo.name, selected = false, baseSpecial = specialTree))
      })
      .getOrElse({
        Logger.error(s"Cannot find specialist: $name")
        None
      })
  }


  /**
    * Gets all possible selectable specialists for the given troop
    *
    * @param troopDto the troop for which to get the specialist
    * @return
    */
  def getPossibleSpecialistsForTroop(troopDto: KTArmyTroopDto): KTSpecialistListOption = {
    val specialistsForTroop = KTTroopDao.getTroopByFactionAndName(troopName = troopDto.name, factionName = troopDto.faction)
      .map(troopDo => {
        troopDo.specialists.map(specialistDo => {
          // check if the specialist is currently selected by the troop
          val selected = troopDto.specialist.exists(_.name == specialistDo.name)

          // we only need to calculate the tree when the special is selected
          val specialTree = if (selected) {
            Some(findSpecialByLevel(specialistDo, 1, StringUtils.EMPTY).head)
          } else {
            None
          }
          KTSpecialistOptionDto(name = specialistDo.name, selected = selected, baseSpecial = specialTree)
        })
          .toList
          .sortBy(_.name)
      })
      .getOrElse({
        Logger.error(s"Troop: ${troopDto.name} not found in faction: ${troopDto.faction}")
        List()
      })


    KTSpecialistListOption(noneSelected = troopDto.specialist.isEmpty,
      specialists = specialistsForTroop)
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

case class KTSpecialistListOption(noneSelected: Boolean,
                                  specialists: List[KTSpecialistOptionDto])

case class KTSpecialistOptionDto(selected: Boolean,
                                 name: String,
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
  * @param specials the specials the specialist currently has
  */
case class KTSpecialistDto(name: String,
                           specials: List[KTSpecialDto])

/**
  * Represents a special a troop can have
  *
  * @param name  the name of the special
  * @param level the level when this special was aquired
  */
case class KTSpecialDto(name: String,
                        level: Int)
