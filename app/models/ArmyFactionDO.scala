package models

import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import play.api.Logger

import scala.collection.JavaConversions._

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 21:14
  */
object ArmyFactionDAO {


  private val FINDER = new Model.Finder[Long, ArmyFactionDO](classOf[ArmyFactionDO])


  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[ArmyFactionDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }

  def addFaction(name:String) : ArmyFactionDO = {

    Logger.info("Adding Faction: "+name+" to database")

    val factionDo = new ArmyFactionDO
    factionDo.name = name
    factionDo.save()
    factionDo
  }
}

@Entity
@Table(name = "faction") class ArmyFactionDO extends Model {

  @Id val id: Long = 0L

  @NotNull
  @Column(unique = true) var name: String = ""

  @OneToMany val troops: java.util.List[ArmyTroopDO] = null
}