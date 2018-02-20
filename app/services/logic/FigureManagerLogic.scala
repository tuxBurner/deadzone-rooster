package services.logic

object FigureManagerLogic {

  case class ManagedFigureDto(faction: String,
                           troopName: String,
                           amount: Int)

  case class ManagedFiguresHolderDto(figures: List[ManagedFigureDto])

  def exportCurrentArmyAsFigureList(army: ArmyDto): ManagedFiguresHolderDto = {
    val figures = army.troopsWithAmount.groupBy(amountTroop => {
     amountTroop.troop.faction+"|"+amountTroop.troop.name
    })
    .map(entry => {
      val factionNameSplit = entry._1.split('|')
      val amount = entry._2.map(_.amount).sum
      ManagedFigureDto(faction = factionNameSplit(0), troopName = factionNameSplit(1), amount = amount)
    }).toList

    ManagedFiguresHolderDto(figures = figures)
  }

  

}
