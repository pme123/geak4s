package pme123.geak4s.services

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom
import pme123.geak4s.domain.Address

/** Service for Swiss Geo Admin API address search */
object GeoAdminService:

  /** Search for addresses using the Swiss Geo Admin API */
  def searchAddress(searchText: String): Future[List[AddressResult]] =
    if searchText.trim.isEmpty then
      Future.successful(List.empty)
    else
      val url = s"https://api3.geo.admin.ch/rest/services/api/SearchServer?searchText=${js.URIUtils.encodeURIComponent(searchText)}&type=locations&limit=10"
      
      dom.fetch(url)
        .toFuture
        .flatMap { response =>
          if response.ok then
            response.text().toFuture.map { text =>
              val json = js.JSON.parse(text)
              parseSearchResponse(json)
            }
          else
            Future.successful(List.empty)
        }
        .recover { case _ =>
          List.empty
        }

  /** Parse the JSON response from Geo Admin API */
  private def parseSearchResponse(json: js.Any): List[AddressResult] =
    try
      val response = json.asInstanceOf[GeoAdminResponse]
      if response.results != null then
        response.results.toList.map { result =>
          // Remove HTML tags from label
          val cleanLabel = result.attrs.label.replace("<b>", "").replace("</b>", "")

          // Parse label: "Sonnenweg 23 6414 Oberarth" or "Sonnenweg 23, 6414 Oberarth"
          val (street, houseNumber, zipCode, city) = parseAddressLabel(cleanLabel)

          AddressResult(
            label = result.attrs.label,
            street = street,
            houseNumber = houseNumber,
            zipCode = zipCode,
            city = city,
            country = Some("Schweiz"),
            lat = result.attrs.lat,
            lon = result.attrs.lon
          )
        }
      else
        List.empty
    catch
      case _: Exception =>
        List.empty

  /** Parse address label into components
    * Format: "Sonnenweg 23 6414 Oberarth" or "Sonnenweg 23, 6414 Oberarth"
    * Returns: (street, houseNumber, zipCode, city)
    */
  private def parseAddressLabel(label: String): (Option[String], Option[String], Option[String], Option[String]) =
    if label.isEmpty then
      (None, None, None, None)
    else
      // Remove comma if present and split by whitespace
      val parts = label.replace(",", "").trim.split("\\s+")

      if parts.length >= 4 then
        // Try to find ZIP code (4 digits)
        val zipIndex = parts.indexWhere(p => p.length == 4 && p.forall(_.isDigit))

        if zipIndex >= 2 then
          // We have at least street + houseNumber before ZIP
          val streetParts = parts.take(zipIndex - 1)
          val houseNumber = parts(zipIndex - 1)
          val zipCode = parts(zipIndex)
          val cityParts = parts.drop(zipIndex + 1)

          (
            Some(streetParts.mkString(" ")),
            Some(houseNumber),
            Some(zipCode),
            if cityParts.nonEmpty then Some(cityParts.mkString(" ")) else None
          )
        else
          // No valid ZIP found, return as is
          (Some(label), None, None, None)
      else
        // Not enough parts, return as is
        (Some(label), None, None, None)

  /** Convert AddressResult to Address domain object */
  def toAddress(result: AddressResult): Address =
    Address(
      street = result.street,
      houseNumber = result.houseNumber,
      zipCode = result.zipCode,
      city = result.city,
      country = result.country
    )

end GeoAdminService

/** Address search result */
case class AddressResult(
  label: String,
  street: Option[String],
  houseNumber: Option[String],
  zipCode: Option[String],
  city: Option[String],
  country: Option[String],
  lat: Double,
  lon: Double
)

/** JSON response structure from Geo Admin API */
@js.native
trait GeoAdminResponse extends js.Object:
  val results: js.Array[GeoAdminResult] = js.native
end GeoAdminResponse

@js.native
trait GeoAdminResult extends js.Object:
  val attrs: GeoAdminAttrs = js.native
end GeoAdminResult

@js.native
trait GeoAdminAttrs extends js.Object:
  val label: String = js.native
  val detail: String = js.native
  val zip: js.UndefOr[Int] = js.native
  val origin: String = js.native
  val lat: Double = js.native
  val lon: Double = js.native
end GeoAdminAttrs

