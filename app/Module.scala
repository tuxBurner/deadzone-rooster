import com.google.inject.AbstractModule
import deadzone.DZDataInitializer
import deadzone.parsers.{CSVArmyImporter, CSVItemsImporter, CSVWeaponImporter}
import killteam.KTDataInitializer
import killteam.parsers._

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  *
  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */
class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[CSVItemsImporter]).asEagerSingleton()
    bind(classOf[CSVArmyImporter]).asEagerSingleton()
    bind(classOf[CSVWeaponImporter]).asEagerSingleton()
    bind(classOf[DZDataInitializer]).asEagerSingleton()

    bind(classOf[KTCSVArmyParser]).asEagerSingleton()
    bind(classOf[KTCSVItemParser]).asEagerSingleton()
    bind(classOf[KTCSVItemParser]).asEagerSingleton()
    bind(classOf[KTCSVLoadoutParser]).asEagerSingleton()
    bind(classOf[KTCSVSpecialistsParser]).asEagerSingleton()
    bind(classOf[KTCSVTacticsParser]).asEagerSingleton()
    bind(classOf[KTDataInitializer]).asEagerSingleton()
  }

}
