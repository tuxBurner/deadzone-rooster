package killteam.logic

object KTPdfArmyDataLogic {

  def getAbilities(armyDto: KTArmyDto) : List[KTAbilityDto] = {
    armyDto
      .troops.flatMap(troopDto => troopDto.abilities)
      .groupBy(_.name)
      .map(_._2.head)
      .toList
      .sortBy(_.name)
  }

}
