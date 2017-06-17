package services.logic

/**
  * Validates the army
  */
object ArmyValidatorLogic {

  /**
    * Validates the army if it is conform with the rules
    * @param army
    */
  def validateArmy(army:ArmyDto) : List[String] = {
    checkSingleFaction(army)
  }

  /**
    * Checks if the army is a single faction army
    * @param army
    * @return
    */
  private def checkSingleFaction(army: ArmyDto) : List[String] = {
    if(army.troops.map(_.faction).distinct.length  > 1) {
      List("Not a single faction Army");
    } else {
      List()
    }
  }

}
