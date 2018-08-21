package deadzone.logic

import models.FactionDAO

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  */
object FactionLogic {

  def getAllFactions():List[FactionDto] = {
    FactionDAO.getAll().map(faction => new FactionDto(faction.name))
  }

}

case class FactionDto(name:String)


