package pme123.geak4s.domain.envelope

/** Böden - Floors */
case class Floor(
  code: String,
  description: String,
  floorType: String,
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

object Floor:
  lazy val example: Floor = Floor(
    code = "Bo-1",
    description = "Kellerdecke ungedämmt",
    floorType = "Gegen Unbeheizt (Keller im Erdreich) (ungedämmt und/oder undicht)",
    renovationYear = None,
    area = 95.0,
    uValue = 1.0,
    bFactor = 0.6,
    quantity = 1,
    floorHeating = false,
    neighborRoomTemp = Some(12.0),
    generalCondition = Some("abgenutzt"),
    priority = Some("Mittlere Priorität: Umsetzung in 2-5 Jahren"),
    possibleImprovements = Some("Dämmung Kellerdecke 10cm"),
    maintenanceCost = Some(200.0),
    investment = Some(8500.0),
    calculationBase = Some("Pro m²"),
    usefulLife = Some(40)
  )
  
  lazy val exampleRenovated: Floor = Floor(
    code = "Bo-2",
    description = "Kellerdecke gedämmt",
    floorType = "Gegen Unbeheizt (K. teilw. im Erdreich) (gedämmt, luftdicht)",
    renovationYear = Some(2012),
    area = 95.0,
    uValue = 0.30,
    bFactor = 0.6,
    quantity = 1,
    floorHeating = false,
    neighborRoomTemp = Some(12.0),
    generalCondition = Some("gebraucht"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None,
    maintenanceCost = Some(100.0),
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(40)
  )
end Floor

