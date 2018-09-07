package killteam.logic

import killteam.models.{KTSpecialistDao, KTSpecialistDo, KTTroopDao}
import org.apache.commons.lang3.StringUtils
import play.api.Logger


/**
  * Logic handling specialist data
  */
object KTSpecialistLogic {


  /**
    * Sets a special at the given troop
    *
    * @param specialName  the name of the special to set
    * @param specialLevel the level of the special to set
    * @param uuid         the uuid of the troop where to set the special on
    * @param armyDto      the army containing the troop
    * @return
    */
  def setSpecialAtTroop(specialName: String, specialLevel: Int, uuid: String, armyDto: KTArmyDto): KTArmyDto = {

    Logger.info(s"Setting special: $specialName level: $specialLevel troop: $uuid")

    KTArmyLogic.getTroopFromArmyByUUIDAndPerformChanges(uuid = uuid, armyDto = armyDto, troopDto => {

      // check if the troop has a specialist
      troopDto.specialist.map(specialist => {

        // check if the level is okay to set
        if (troopDto.level != specialLevel - 1 && specialLevel > 4) {
          Logger.error(s"Level troop: ${troopDto.level} does not fit the required level: $specialLevel")
          None
        } else {
          // get the special and the set it at the troop
          KTSpecialistDao.findSpecialistByName(troopDto.specialist.get.name)
            .map(specialistDo => {

              // check if the special is already set at the troop
              if (specialist.selectedSpecials.exists(special => special.level == specialLevel && special.name == specialName)) {
                Logger.warn(s"Special: $specialName level: $specialLevel already set at troop: $uuid")
                return armyDto
              }

              // find the special in the specials we need some different handling on level4
              val specialDoOptional = if (specialLevel < 4) {
                specialistDo.specials.find(special => special.name == specialName && special.level == specialLevel)
              } else {
                specialistDo.specials.find(special => special.name == specialName && !troopDto.specialist.get.selectedSpecials.exists(_.name == specialName))
              }

              // find the special in the specialist
              specialDoOptional
                .map(specialDo => {
                  // the newspecials list remove all the specials which are higher than the new one and add the new one
                  val newSpecials = specialist.selectedSpecials.filter(_.level <= specialLevel) :+ KTSpecialTroopDto(name = specialDo.name, level = specialLevel)

                  // filter out all speciales which are on the same level and dont have the same name
                  val cleanedSpecials = newSpecials.filter(special => special.level != specialLevel || (special.level == specialLevel && special.name == specialName))

                  // recreate the troop and add the new special to the list
                  val newTroop = troopDto.copy(level = specialLevel, specialist = Some(specialist.copy(selectedSpecials = cleanedSpecials)))
                  Some(newTroop)
                })
                .getOrElse({
                  Logger.error(s"Cannot find special: $specialName level: $specialLevel for specialist: ${specialistDo.name}")
                  None
                })
            })
            .getOrElse({
              Logger.error(s"Could not find sepcialist: ${troopDto.specialist}")
              None
            })
        }
      }).getOrElse({
        Logger.error(s"Troop: ${troopDto.uuid} has no specialist set so cannot set any specail at it")
        None
      })
    })
  }

  /**
    * Sets the given level at the given troop and resets all specials to fit the selected level
    *
    * @param uuid    the uuid of the troop to set the level on
    * @param level   the level to set
    * @param armyDto the army containing the troop
    * @return
    */
  def setLevelAtTroop(uuid: String, level: Int, armyDto: KTArmyDto): KTArmyDto = {
    KTArmyLogic.getTroopFromArmyByUUIDAndPerformChanges(uuid = uuid, armyDto = armyDto, troopDto => {
      // nothing  when smaller or 1 when setting level
      if (level <= 1 || troopDto.specialist.isEmpty) {
        Some(troopDto)
      }
      val newSpecials = troopDto.specialist.get.selectedSpecials.filter(special => special.level <= level)
      Some(troopDto.copy(level = level, specialist = Some(troopDto.specialist.get.copy(selectedSpecials = newSpecials))))
    })
  }


  /**
    * Gets the possible specialist options for the given troop type and army
    *
    * @param troopName   the name of the troop to get the specials for
    * @param factionName the name of the faction
    * @param armyDto     the army where to check for which specials are avaible
    * @return
    */
  def getAvaibleSpecialistSelectOptions(troopName: String, factionName: String, armyDto: KTArmyDto): List[String] = {

    // only 4 specialist allowed
    if (armyDto.troops.count(_.specialist.isDefined) >= 4) {
      return List("")
    }

    val specialistsForTroop = KTTroopDao.getTroopByFactionAndName(troopName = troopName, factionName = factionName)
      .map(_.specialists.map(_.name).toList)
      .getOrElse(List("None"))

    val returnList = "" :: specialistsForTroop
    returnList.sorted
  }

  /**
    * Gets the specialist option by the name of the specialist
    *
    * @param name                  the name of the specialist
    * @param troopSelectedSpecials the specials selected by the troop
    * @return
    */
  def getSpecialistOptionByName(name: String, troopSelectedSpecials: List[KTSpecialTroopDto]): Option[KTSpecialistOptionDto] = {
    KTSpecialistDao.findSpecialistByName(name)
      .map(specialistDo => {
        val specialTree = Some(findSpecialByLevel(specialistDo, 1, StringUtils.EMPTY, troopSelectedSpecials).head)
        Some(KTSpecialistOptionDto(name = specialistDo.name, baseSpecial = specialTree))
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

    if (troopDto.specialist.isEmpty) {
      return None
    }

    getSpecialistOptionByName(troopDto.specialist.get.name, troopDto.specialist.get.selectedSpecials)
  }

  /**
    * Gets the specialist for setting it at the troop
    *
    * @param specialistName the name of the specialist to set
    * @return
    */
  def getSpecialistForTroop(specialistName: String): Option[KTSpecialistTroopDto] = {
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
    * @param specialistDo          the specialist to traverse the specials from
    * @param level                 the level to get the specials for
    * @param requireSpecialName    the parent special
    * @param troopSelectedSpecials the specials which are selected by the troop
    * @return
    */
  private def findSpecialByLevel(specialistDo: KTSpecialistDo, level: Int, requireSpecialName: String, troopSelectedSpecials: List[KTSpecialTroopDto]): List[KTSpecialOptionDto] = {
    specialistDo.specials.filter(specialDo => specialDo.level == level && specialDo.require == requireSpecialName)
      .map(specialDo => {
        // find sub specials for the special
        val subSpecials = if (level == 4) {
          List()
        } else if (level == 3) {
          // get all specials which are not selected for level 4
          specialistDo.specials
            .filterNot(specialDo => troopSelectedSpecials.exists(troopSpecial => troopSpecial.name == specialDo.name && troopSpecial.level != 4))
            .map(specialDo => {
              val selected = troopSelectedSpecials.exists(troopSpecial => troopSpecial.name == specialDo.name && troopSpecial.level == 4)
              KTSpecialOptionDto(selected = selected,
                level = 4,
                name = specialDo.name,
                subSpecials = List())
            })
            .toList
            .sortBy(_.name)
        } else {
          findSpecialByLevel(specialistDo, level + 1, specialDo.name, troopSelectedSpecials)
        }

        val selected = troopSelectedSpecials.exists(troopSpecial => troopSpecial.name == specialDo.name && troopSpecial.level == level)
        val selectable = level != 1

        KTSpecialOptionDto(selected = selected,
          level = level,
          name = specialDo.name,
          subSpecials = subSpecials)
      })
      .toList
      .sortBy(_.name)
  }

}


case class KTSpecialistOptionDto(name: String,
                                 baseSpecial: Option[KTSpecialOptionDto])

case class KTSpecialOptionDto(selected: Boolean,
                              name: String,
                              level: Int,
                              subSpecials: List[KTSpecialOptionDto] = List())

/**
  * Represents a specialist of a troop
  *
  * @param name             the name of the specialist
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
