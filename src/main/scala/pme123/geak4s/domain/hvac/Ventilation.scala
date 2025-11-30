package pme123.geak4s.domain.hvac

/** Lüftung - Ventilation */
case class Ventilation(
  code: String,
  description: String,
  usageType: String,
  ventilationType: String,
  commissioningYear: Option[Int],
  maintenanceCost: Double,
  quantity: Int,
  roomsOrPersons: Option[Int],
  heatRecoveryType: Option[String],
  fanType: Option[String],
  airFlowRate: Option[Double],
  thermalAirRate: Option[Double],
  electricityDemandVentilation: Option[Double],
  electricityDemandCooling: Option[Double],
  electricityDemandClimate: Option[Double],
  priority: Option[String],
  possibleImprovements: Option[String],
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object Ventilation:
  lazy val example: Ventilation = Ventilation(
    code = "LU-1",
    description = "Komfortlüftung mit WRG",
    usageType = "Mehrfamilienhaus",
    ventilationType = "Lüftungsanlage mit Lufterwärmung (WRG, mit Zu- und Abluft)",
    commissioningYear = Some(2015),
    maintenanceCost = 800.0,
    quantity = 1,
    roomsOrPersons = Some(12),
    heatRecoveryType = Some("Gegenstrom-Wärmetauscher"),
    fanType = Some("DC/EC-Motor"),
    airFlowRate = Some(0.7),
    thermalAirRate = Some(850.0),
    electricityDemandVentilation = Some(2500.0),
    electricityDemandCooling = None,
    electricityDemandClimate = None,
    priority = Some("Keine Priorität"),
    possibleImprovements = None,
    investment = None,
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(20)
  )
  
  lazy val exampleSimple: Ventilation = Ventilation(
    code = "LU-2",
    description = "Abluftanlage Bad/WC",
    usageType = "Mehrfamilienhaus",
    ventilationType = "Einfache Abluftanlage (ohne WRG)",
    commissioningYear = Some(2000),
    maintenanceCost = 200.0,
    quantity = 12,
    roomsOrPersons = None,
    heatRecoveryType = Some("Keine Wärmerückgewinnung"),
    fanType = Some("AC-Motor"),
    airFlowRate = None,
    thermalAirRate = None,
    electricityDemandVentilation = Some(500.0),
    electricityDemandCooling = None,
    electricityDemandClimate = None,
    priority = Some("Geringe Priorität: Umsetzung in 5-10 Jahren"),
    possibleImprovements = Some("Ersatz durch Lüftung mit WRG"),
    investment = Some(15000.0),
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(20)
  )
end Ventilation

