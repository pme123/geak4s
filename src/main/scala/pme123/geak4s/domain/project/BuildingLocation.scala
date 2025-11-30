package pme123.geak4s.domain.project

/** Building location */
case class BuildingLocation(
  street: String,
  houseNumber: Option[String],
  zipCode: String,
  city: String,
  municipality: String,
  canton: String,
  weatherStation: String,
  altitude: Option[Double],
  coordinates: Option[Coordinates]
)

object BuildingLocation:
  lazy val example: BuildingLocation = BuildingLocation(
    street = "Musterstrasse",
    houseNumber = Some("123"),
    zipCode = "8000",
    city = "Zürich",
    municipality = "Zürich",
    canton = "ZH",
    weatherStation = "Zürich-Fluntern",
    altitude = Some(556.0),
    coordinates = Some(Coordinates.example)
  )
end BuildingLocation

case class Coordinates(
  latitude: Double,
  longitude: Double
)

object Coordinates:
  lazy val example: Coordinates = Coordinates(
    latitude = 47.3769,
    longitude = 8.5417
  )
end Coordinates

