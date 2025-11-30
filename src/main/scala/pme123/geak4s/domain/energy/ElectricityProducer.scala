package pme123.geak4s.domain.energy

/** Elektrizitätsprod. - Electricity Producer (PV, CHP) */
case class ElectricityProducer(
  code: String,
  producerType: String,
  connectedHeatProducer: Option[String],
  description: String,
  commissioningYear: Option[Int],
  annualProduction: Double,
  gridFeedIn: Double,
  maintenanceCost: Double,
  selfConsumptionCalculatedExternally: Boolean,
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int],
  generalCondition: Option[String],
  priority: Option[String],
  possibleImprovements: Option[String]
)

object ElectricityProducer:
  lazy val examplePV: ElectricityProducer = ElectricityProducer(
    code = "EP-1",
    producerType = "PV",
    connectedHeatProducer = None,
    description = "Photovoltaikanlage Dach Süd",
    commissioningYear = Some(2019),
    annualProduction = 12500.0,
    gridFeedIn = 60.0,
    maintenanceCost = 300.0,
    selfConsumptionCalculatedExternally = false,
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30),
    generalCondition = Some("neuwertig"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None
  )
  
  lazy val exampleCHP: ElectricityProducer = ElectricityProducer(
    code = "EP-2",
    producerType = "WKK",
    connectedHeatProducer = Some("WE-4"),
    description = "Blockheizkraftwerk",
    commissioningYear = Some(2018),
    annualProduction = 8500.0,
    gridFeedIn = 20.0,
    maintenanceCost = 1200.0,
    selfConsumptionCalculatedExternally = false,
    investment = None,
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(15),
    generalCondition = Some("gut"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None
  )
  
  lazy val examplePVPlanned: ElectricityProducer = ElectricityProducer(
    code = "EP-3",
    producerType = "PV",
    connectedHeatProducer = None,
    description = "Photovoltaikanlage Dach Ost/West (geplant)",
    commissioningYear = None,
    annualProduction = 8000.0,
    gridFeedIn = 50.0,
    maintenanceCost = 250.0,
    selfConsumptionCalculatedExternally = false,
    investment = Some(25000.0),
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30),
    generalCondition = None,
    priority = Some("Mittlere Priorität: Umsetzung in 2-5 Jahren"),
    possibleImprovements = Some("Installation PV-Anlage mit Batteriespeicher")
  )
end ElectricityProducer

