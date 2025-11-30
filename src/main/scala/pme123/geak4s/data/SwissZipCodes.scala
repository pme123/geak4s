package pme123.geak4s.data

import scala.scalajs.js
import org.scalajs.dom
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/** Swiss ZIP codes database */
object SwissZipCodes:

  case class City(
    zip: String,
    name: String,
    canton: String,
    coordEst: Double,
    coordNorth: Double,
    language: String
  )

  // Mutable list that will be populated from CSV
  private var cities: List[City] = List.empty

  /** Load cities from CSV file */
  def loadCities(): Future[Unit] =
    dom.fetch("/CitiesCH.csv")
      .toFuture
      .flatMap { response =>
        if response.ok then
          response.text().toFuture
        else
          Future.failed(new Exception(s"Failed to load CSV: ${response.statusText}"))
      }
      .map { csvText =>
        parseCsv(csvText)
      }
      .recover { case ex: Exception =>
        dom.console.error(s"Error loading cities: ${ex.getMessage}")
      }

  /** Parse CSV text into City objects */
  private def parseCsv(csvText: String): Unit =
    try
      val lines = csvText.split("\n").toList
      // Skip header line
      val dataLines = lines.drop(1)

      cities = dataLines.flatMap { line =>
        try
          val parts = line.split(";")
          if parts.length >= 9 then
            Some(City(
              zip = parts(1).trim,
              name = parts(0).trim,
              canton = parts(5).trim,
              coordEst = parts(6).trim.toDouble,
              coordNorth = parts(7).trim.toDouble,
              language = parts(8).trim
            ))
          else None
        catch
          case _: Exception => None
      }

      dom.console.log(s"Loaded ${cities.length} Swiss cities from CSV")
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing CSV: ${ex.getMessage}")
        cities = List.empty

  /** Find city by ZIP code */
  def findCityByZip(zip: String): Option[String] =
    cities.find(_.zip == zip).map(_.name)

  /** Find ZIP code by city name (returns first match) */
  def findZipByCity(cityName: String): Option[String] =
    cities.find(_.name.equalsIgnoreCase(cityName)).map(_.zip)

  /** Search ZIP codes by partial match */
  def searchByZip(partial: String): List[City] =
    if partial.isEmpty then List.empty
    else cities.filter(_.zip.startsWith(partial)).take(10)

  /** Search cities by partial match */
  def searchByCity(partial: String): List[City] =
    if partial.isEmpty then List.empty
    else cities.filter(_.name.toLowerCase.contains(partial.toLowerCase)).take(10)


end SwissZipCodes

