package pme123.geak4s.domain.envelope

/** Wände - Walls */
case class Wall(
  code: String,
  description: String,
  wallType: String,
  orientation: Option[String],
  renovationYear: Option[Int],
  area: Double,
  uValue: Double,
  bFactor: Double,
  quantity: Int,
  wallHeating: Boolean,
  neighborRoomTemp: Option[Double],
  generalCondition: Option[String],
  priority: Option[String],
  possibleImprovements: Option[String],
  maintenanceCost: Option[Double],
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object Wall:
  lazy val example: Wall = Wall(
    code = "W-1",
    description = "Aussenwand ungedämmt",
    wallType = "Aussenwand",
    orientation = Some("N"),
    renovationYear = None,
    area = 180.5,
    uValue = 1.5,
    bFactor = 1.0,
    quantity = 1,
    wallHeating = false,
    neighborRoomTemp = None,
    generalCondition = Some("abgenutzt"),
    priority = Some("Hohe Priorität: Umsetzung in < 2 Jahren"),
    possibleImprovements = Some("Aussendämmung 16cm"),
    maintenanceCost = Some(800.0),
    investment = Some(45000.0),
    calculationBase = Some("Pro m²"),
    usefulLife = Some(40)
  )
  
  lazy val exampleRenovated: Wall = Wall(
    code = "W-2",
    description = "Aussenwand gedämmt",
    wallType = "Aussenwand",
    orientation = Some("S"),
    renovationYear = Some(2015),
    area = 150.0,
    uValue = 0.20,
    bFactor = 1.0,
    quantity = 1,
    wallHeating = false,
    neighborRoomTemp = None,
    generalCondition = Some("neuwertig"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None,
    maintenanceCost = Some(300.0),
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(40)
  )
end Wall

