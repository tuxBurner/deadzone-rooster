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
object FactionDAO {


  private val FINDER = new Model.Finder[Long, FactionDO](classOf[FactionDO])

  def getAll(): List[FactionDO] = {
    FINDER.orderBy().asc("name").findList().toList
  }

  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[FactionDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }

  def countAll(): Int = {
    FINDER.findRowCount()
  }

  def addFaction(name:String) : FactionDO = {

    Logger.info("Adding Faction: "+name+" to database")

    val factionDo = new FactionDO
    factionDo.name = name
    factionDo.save()
    factionDo
  }
}

@Entity
@Table(name = "faction") class FactionDO extends Model {

  @Id val id: Long = 0L

  @NotNull
  @Column(unique = true) var name: String = ""

  @OneToMany val troops: java.util.List[TroopDO] = null
}