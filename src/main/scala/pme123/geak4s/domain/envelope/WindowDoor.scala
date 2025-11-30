package pme123.geak4s.domain.envelope

/** Fenster und Türen - Windows and Doors */
case class WindowDoor(
  code: String,
  description: String,
  windowType: String,
  orientation: Option[String],
  renovationYear: Option[Int],
  area: Double,
  uValue: Double,
  gValue: Double,
  bFactor: Double,
  glassRatio: Double,
  shading: Double,
  quantity: Int,
  installedIn: Option[String],
  generalCondition: Option[String],
  priority: Option[String],
  possibleImprovements: Option[String],
  maintenanceCost: Option[Double],
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object WindowDoor:
  lazy val example: WindowDoor = WindowDoor(
    code = "Fe-1",
    description = "Holzfenster 2-fach Verglasung",
    windowType = "Fenster",
    orientation = Some("S"),
    renovationYear = Some(2005),
    area = 45.5,
    uValue = 1.3,
    gValue = 0.60,
    bFactor = 1.0,
    glassRatio = 0.70,
    shading = 0.85,
    quantity = 12,
    installedIn = Some("W-1"),
    generalCondition = Some("gebraucht"),
    priority = Some("Mittlere Priorität: Umsetzung in 2-5 Jahren"),
    possibleImprovements = Some("Ersatz durch 3-fach Verglasung"),
    maintenanceCost = Some(400.0),
    investment = Some(18000.0),
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30)
  )
  
  lazy val exampleNew: WindowDoor = WindowDoor(
    code = "Fe-2",
    description = "Kunststofffenster 3-fach Verglasung",
    windowType = "Fenster",
    orientation = Some("N"),
    renovationYear = Some(2020),
    area = 35.0,
    uValue = 0.80,
    gValue = 0.50,
    bFactor = 1.0,
    glassRatio = 0.75,
    shading = 0.90,
    quantity = 8,
    installedIn = Some("W-2"),
    generalCondition = Some("neuwertig"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None,
    maintenanceCost = Some(200.0),
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30)
  )
  
  lazy val exampleDoor: WindowDoor = WindowDoor(
    code = "Fe-3",
    description = "Eingangstür gedämmt",
    windowType = "Tür",
    orientation = Some("O"),
    renovationYear = Some(2015),
    area = 2.5,
    uValue = 1.0,
    gValue = 0.0,
    bFactor = 1.0,
    glassRatio = 0.20,
    shading = 1.0,
    quantity = 1,
    installedIn = Some("W-1"),
    generalCondition = Some("neuwertig"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None,
    maintenanceCost = Some(50.0),
    investment = None,
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(30)
  )
end WindowDoor

