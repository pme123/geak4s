package pme123.geak4s.domain.project

/** Gebäude - Building location and identification */
case class BuildingLocation(
  zipCode: Option[String],              // PLZ
  city: Option[String],                 // Ort
  municipality: Option[String],         // Gemeinde
  street: Option[String],               // Strasse
  houseNumber: Option[String],          // Hausnummer
  buildingName: Option[String],         // Gebäudebezeichnung
  parcelNumber: Option[String]          // Parzellen-Nummer
)

object BuildingLocation:
  lazy val example: BuildingLocation = BuildingLocation(
    zipCode = Some("8000"),
    city = Some("Zürich"),
    municipality = Some("Zürich"),
    street = Some("Musterstrasse"),
    houseNumber = Some("123"),
    buildingName = Some("Wohnhaus Musterstrasse"),
    parcelNumber = Some("1234")
  )
end BuildingLocation

