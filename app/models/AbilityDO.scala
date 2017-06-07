package models

import javax.persistence.{Column, Entity, Id, Table}
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.Models
import org.apache.commons.lang3.StringUtils
import play.api.Logger

import scala.collection.JavaConversions._

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 22:50
  */
object AbilityDAO {

  private val FINDER = new Model.Finder[Long, AbilityDO](classOf[AbilityDO])

  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[AbilityDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }

  def addByAbilityDtos(abilityDto: Models.AbilityDto): AbilityDO = {
    findOrAddByName(abilityDto.title, abilityDto.factor != 0)
  }


  def findOrAddByName(name: String, incValue: Boolean): AbilityDO = {

    if (StringUtils.isEmpty(name)) {
      return null;
    }

    val dbDo = FINDER.where().ieq("name", name).findUnique()
    if (dbDo != null) {
      return dbDo
    }

    Logger.info("Creating ability: " + name + " in database")
    val newDbDo = new AbilityDO()
    newDbDo.name = name
    newDbDo.hasIncVal = incValue
    newDbDo.save()
    newDbDo
  }
}

@Entity
@Table(name = "ability") class AbilityDO extends Model {

  @Id val id: Long = 0L

  @NotNull
  @Column(unique = true) var name: String = ""

  /**
    * When true this means the value of the ability can be incremented.
    * Tactician (2)
    * When false this meas the value cannot be changed.
    */
  @NotNull var hasIncVal: Boolean = false

}
