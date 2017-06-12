package services.logic

import models.FactionDAO

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 12.06.17
  *         Time: 13:52
  */
object FactionLogic {

  def getAllFactions():List[FactionDto] = {
    FactionDAO.getAll().map(faction => new FactionDto(faction.name))
  }

}

case class FactionDto(name:String)


