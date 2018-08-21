package deadzone.parsers

import java.io.File

import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
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
    * @param path       of the csv to read
    * @param rosterType the type of the roster
    * @return the parsed csv file as a List 
    */
  def readCsvFile(path: String, rosterType: String): List[Map[String, String]] = {

    val configuredExternalFolder = CSVDataParser.checkAndGetExternalConfigFolder(configuration, rosterType)

    implicit object MyFormat extends DefaultCSVFormat {
      override val delimiter = ';'
    }

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


  /**
    * Splits the given string by , and trims the results
    *
    * @param stringToSplit the string to split
    * @return an [[Array[String]]
    */
  def splitStringByCommaAndTrim(stringToSplit: String): Array[String] = {
    stringToSplit.split(',').map(_.trim)
  }

  def getSetFromLine(columnName: String, lineData: Map[String, String], emptyOkay: Boolean = false): Set[String] = {
    getDataFromLine(columnName, lineData, emptyOkay)
      .split(',')
      .map(_.trim)
      .filter(StringUtils.isNotBlank(_))
      .toSet
  }

  def getIntFromLine(columnName: String, lineData: Map[String, String], emptyOkay: Boolean = false): Int = {
    val cleanedField = getDataFromLine(columnName, lineData, emptyOkay)
      .replace("+", "")
      .replace("\"", "")

    if (StringUtils.isBlank(cleanedField)) {
      0
    } else {
      cleanedField.toInt
    }
  }

  def getDataFromLine(columnName: String, lineData: Map[String, String], emptyOkay: Boolean = false): String = {
    val columnVal = lineData.get(columnName)
    if (columnVal.isEmpty) {
      Logger.error(s"No data found in column: $columnName in line: $lineData")
      throw CSVDataEmptyException()
    }

    if (columnVal.isDefined && StringUtils.isBlank(columnVal.get) && !emptyOkay) {
      Logger.error(s"Data for column: $columnName is empty in line: $lineData")
      throw CSVDataEmptyException()
    }

    columnVal.get
  }
}

final case class CSVDataEmptyException() extends Exception()

object CSVDataParser {
  /**
    * Checks if in the configuration is an external folder specified for the configurations.
    * This feature can be used to create and test the configurations.
    *
    * @return
    */
  def checkAndGetExternalConfigFolder(configuration: Configuration, rosterType: String): Option[File] = {
    configuration.getOptional[String](f"$rosterType.externalConfigFolder")
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
