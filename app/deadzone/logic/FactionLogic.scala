package deadzone.logic

import deadzone.models.FactionDAO

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  */
object FactionLogic {

  def getAllFactions():List[FactionDto] = {
    FactionDAO.getAll().map(faction => new FactionDto(faction.name))
  }

}

case class FactionDto(name:String)


