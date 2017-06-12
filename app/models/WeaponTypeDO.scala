package models

import javax.persistence.{Column, Entity, Id, Table}
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import play.api.Logger

import scala.collection.JavaConversions._


object WeaponTypeDAO {

  private val FINDER = new Model.Finder[Long, WeaponTypeDO](classOf[WeaponTypeDO])


  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[WeaponTypeDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }

  def findOrCreateTypeByName(name: String): WeaponTypeDO = {
    val typeDbDo = FINDER.where().ieq("name",name).findUnique
    if(typeDbDo != null) {
      return typeDbDo
    }

    val newTypeDo = new WeaponTypeDO();
    newTypeDo.name = name;
    newTypeDo.save()
    return newTypeDo
  }

}



@Entity
@Table(name = "weapon_type")
class WeaponTypeDO extends Model {

  @Id
  val id:Long  = 0L

  @NotNull
  @Column(unique = true)
  var name:String = ""
}
