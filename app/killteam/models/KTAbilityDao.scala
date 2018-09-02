package killteam.models

import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Handles all data access to the ability database
  */
object KTAbilityDao {
  /**
    * All killteam abillities
    */
  val abbilities: ListBuffer[KTAbilityDo] = ListBuffer()


  /**
    * Adds an item to the given faction
    *
    * @param csvItemDtothe item information's from the csv
    * @param factionDo     the faction the weapon belongs to
    * @return
    */
  def getOrAddAbilityToFaction(abilityName: String, factionDo: KTFactionDo): KTAbilityDo = {
    abbilities.find(ability => ability.name == abilityName && ability.faction.name == factionDo.name)
      .getOrElse({
        Logger.info(s"KT adding ability: ${abilityName} to faction: ${factionDo.name}")
        val abilityDo = KTAbilityDo(faction = factionDo,
          name = abilityName)

        abbilities += abilityDo
        abilityDo
      })
  }

  /**
    * Gets all abilities by the given faction name
    * @param factionName the name of the faction to get the abilities for
    * @return
    */
  def getAllAbilitiesByFaction(factionName: String) : List[KTAbilityDo] = {
    abbilities.filter(_.faction.name == factionName).sortBy(_.name).toList
  }
}

/**
  * Represents an ability
  *
  * @param faction the faction the ability belongs to
  * @param name    the name of the ability
  */
case class KTAbilityDo(faction: KTFactionDo,
                       name: String)

