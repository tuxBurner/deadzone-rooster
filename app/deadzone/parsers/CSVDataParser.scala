package deadzone.parsers

import java.io.File

import com.github.tototoshi.csv.CSVReader
import org.apache.commons.lang3.StringUtils
import play.api.{Configuration, Logger}

import scala.io.Source

/**
  * Class for reading csv data from a file
  *
  * @author Sebastian Hardt (s.hardt@micromata.de)
  */
class CSVDataParser(configuration: Configuration) {


  /**
    * Reads the csv from the given path in the classpath
    *
    * @param path of the csv to read
    * @return the parsed csv file as a List 
    */
  def readCsvFile(path: String): List[Map[String, String]] = {

    val configuredExternalFolder = CSVDataParser.checkAndGetExternalConfigFolder(configuration);

    val reader = configuredExternalFolder.map(externalConfFolder => {
      val confFile = new File(externalConfFolder, path)
      Logger.info(s"Reading configuration from: ${confFile.getAbsolutePath}")
      CSVReader.open(confFile)
    }).getOrElse({
      val currentClassLoader = Thread.currentThread.getContextClassLoader
      val csvIs = currentClassLoader.getResourceAsStream(path)
      CSVReader.open(Source.fromInputStream(csvIs))
    })

    reader.allWithHeaders()
  }


}

object CSVDataParser {
  /**
    * Checks if in the configuration is an external folder specified for the configurations.
    * This feature can be used to create and test the configurations.
    *
    * @return
    */
  def checkAndGetExternalConfigFolder(configuration: Configuration): Option[File] = {
    configuration.getOptional[String]("deadzone.externalConfigFolder")
      .map(path => {

        if (StringUtils.isBlank(path)) {
          return Option.empty;
        }

        val externalConfFolder = new File(path)
        if (externalConfFolder.exists && externalConfFolder.isDirectory) {
          Logger.info(s"Found external config folder: ${externalConfFolder.getAbsolutePath}")
          return Option.apply(externalConfFolder)
        }
        Logger.error(s"Could not find configured folder: ${externalConfFolder.getAbsolutePath}")
        return Option.empty
      })
      .getOrElse(Option.empty)
  }
}
