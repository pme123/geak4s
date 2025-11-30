package pme123.geak4s.domain.envelope

/** Dächer und Decken - Roofs and Ceilings */
case class RoofCeiling(
  code: String,
  description: String,
  roofType: String,
  orientation: Option[String],
  renovationYear: Option[Int],
  area: Double,
  uValue: Double,
  bFactor: Double,
  quantity: Int,
  floorHeating: Boolean,
  neighborRoomTemp: Option[Double],
  generalCondition: Option[String],
  priority: Option[String],
  possibleImprovements: Option[String],
  maintenanceCost: Option[Double],
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object RoofCeiling:
  lazy val example: RoofCeiling = RoofCeiling(
    code = "Da-1",
    description = "Schrägdach ungedämmt",
    roofType = "Schrägdach, unbeheizt",
    orientation = Some("S"),
    renovationYear = None,
    area = 120.5,
    uValue = 1.2,
    bFactor = 1.0,
    quantity = 1,
    floorHeating = false,
    neighborRoomTemp = Some(10.0),
    generalCondition = Some("abgenutzt"),
    priority = Some("Hohe Priorität: Umsetzung in < 2 Jahren"),
    possibleImprovements = Some("Dämmung 20cm Mineralwolle"),
    maintenanceCost = Some(500.0),
    investment = Some(25000.0),
    calculationBase = Some("Pro m²"),
    usefulLife = Some(40)
  )
  
  lazy val exampleFlat: RoofCeiling = RoofCeiling(
    code = "Da-2",
    description = "Flachdach gedämmt",
    roofType = "Flachdach/Terrasse",
    orientation = None,
    renovationYear = Some(2010),
    area = 85.0,
    uValue = 0.25,
    bFactor = 1.0,
    quantity = 1,
    floorHeating = false,
    neighborRoomTemp = None,
    generalCondition = Some("gebraucht"),
    priority = Some("Mittlere Priorität: Umsetzung in 2-5 Jahren"),
    possibleImprovements = None,
    maintenanceCost = Some(200.0),
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30)
  )
end RoofCeiling

