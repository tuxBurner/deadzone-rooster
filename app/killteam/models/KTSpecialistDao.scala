package killteam.models

import killteam.parsers.KTCsvSpecialistDto
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Handles all data access to the specialists database
  */
object KTSpecialistDao {
  /**
    * All killteam specialists
    */
  private val specialists: ListBuffer[KTSpecialistDo] = ListBuffer()


  /**
    * Returns all specialists known in the roster
    *
    * @return
    */
  def getAllSpecialists(): List[KTSpecialistDo] = {
    specialists.toList
  }

  /**
    * Finds the specialists by its name
    *
    * @param name the name of the specialist
    * @return
    */
  def findSpecialistByName(name: String): Option[KTSpecialistDo] = {
    specialists.find(_.name == name)
  }

  /**
    * Adds all specialists
    *
    * @param csvSpecialistDtos the specialist information's from the csv
    * @return
    */
  def addSpecialists(csvSpecialistDtos: List[KTCsvSpecialistDto]): Unit = {

    val groupedSpecialists = csvSpecialistDtos.groupBy(_.specialist)

    for ((specialistName, specialistInfos) <- groupedSpecialists) {

      Logger.info(s"KT adding specialist: ${specialistName}")

      val specials = specialistInfos.map(specialistDto => {
        Logger.info(s"KT adding special: ${specialistDto.name} to specialist: $specialistName")
        KTSpecialDo(name = specialistDto.name,
          require = specialistDto.require,
          level = specialistDto.level)
      }).toSet

      specialists += KTSpecialistDo(name = specialistName, specials = specials)
    }
  }
}

/**
  * Represents a specialist from the specialists.csv
  *
  * @param name     the name of the special the specialist has
  * @param specials the specials the specialist can have
  */
case class KTSpecialistDo(name: String, specials: Set[KTSpecialDo])


/**
  * Represents a special a specialist can have
  *
  * @param name    the name of the special the specialist has
  * @param require the name of the reacquired special to acquire this one
  * @param level   the level of the special
  */
case class KTSpecialDo(name: String,
                       require: String,
                       level: Int)



