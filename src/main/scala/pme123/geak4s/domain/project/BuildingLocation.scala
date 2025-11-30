package pme123.geak4s.domain.project

import pme123.geak4s.domain.Address

/** Gebäude - Building location and identification */
case class BuildingLocation(
  address: Address,                     // Address (street, houseNumber, zipCode, city)
  municipality: Option[String],         // Gemeinde
  buildingName: Option[String],         // Gebäudebezeichnung
  parcelNumber: Option[String]          // Parzellen-Nummer
)

object BuildingLocation:
  lazy val example: BuildingLocation = BuildingLocation(
    address = Address.example,
    municipality = Some("Zürich"),
    buildingName = Some("Wohnhaus Musterstrasse"),
    parcelNumber = Some("1234")
  )
end BuildingLocation

