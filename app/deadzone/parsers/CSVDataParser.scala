package deadzone.parsers

import com.github.tototoshi.csv.CSVReader

import scala.io.Source

/**
  * @author Sebastian Hardt (s.hardt@micromata.de)
  *         Date: 19.06.17
  *         Time: 13:14
  */
trait CSVDataParser {

  /**
    * Reads the csv from the given path in the classpath
    * @param path
    * @return
    */
  def readCsvFile(path: String): List[Map[String, String]] =  {
    val currentClassLoader = Thread.currentThread.getContextClassLoader
    val csvIs = currentClassLoader.getResourceAsStream(path)
    val reader = CSVReader.open(Source.fromInputStream(csvIs))
    reader.allWithHeaders()
  }

}
