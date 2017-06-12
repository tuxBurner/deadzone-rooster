package models

import javax.persistence._
import javax.validation.constraints.NotNull

import com.avaje.ebean.Model
import deadzone.models.CSVModels.AbilityDto
import play.api.Logger

import scala.collection.JavaConversions._


object DefaultWeaponAbilityDAO {

  private val FINDER = new Model.Finder[Long, DefaultWeaponAbilityDO](classOf[DefaultWeaponAbilityDO])


  def addAbilityForWeapon(weaponDO: WeaponDO, abilityDto: AbilityDto): Unit = {
    val abilityDO = AbilityDAO.addByAbilityDtos(abilityDto)
    if (abilityDO == null) {
      return
    }

    val DefaulWeaponAbilityDO = new DefaultWeaponAbilityDO()
    DefaulWeaponAbilityDO.ability = abilityDO
    DefaulWeaponAbilityDO.weaponDo = weaponDO
    DefaulWeaponAbilityDO.defaultValue = abilityDto.factor

    DefaulWeaponAbilityDO.save()
  }

  def deleteAll(): Unit = {
    Logger.info("Deleting all: " + classOf[DefaultTroopAbilityDO].getName + " from database")
    FINDER.all().toList.foreach(_.delete())
  }
}

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 07.06.17
  *         Time: 22:57
  */
@Entity
@Table(name = "def_weapon_ability") class DefaultWeaponAbilityDO extends Model {

  @Id val id: Long = 0L

  @ManyToOne var weaponDo: WeaponDO = null

  @ManyToOne var ability: AbilityDO = null

  @NotNull var defaultValue: Int = 0
}
