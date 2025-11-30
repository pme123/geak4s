package pme123.geak4s.domain.hvac

/** Wärmeerzeuger - Heat Producer */
case class HeatProducer(
  code: String,
  description: String,
  energySource: String,
  efficiencyHeating: Double,
  efficiencyHotWater: Double,
  oversizing: Double,
  producerType: String,
  suppliedDistributionSystems: List[String],
  heatEmissionType: String,
  constructionYear: Option[Int],
  condition: Option[String],
  location: String,
  maintenanceCost: Double,
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object HeatProducer:
  lazy val example: HeatProducer = HeatProducer(
    code = "WE-1",
    description = "Ölheizung",
    energySource = "Heizöl",
    efficiencyHeating = 0.85,
    efficiencyHotWater = 0.75,
    oversizing = 1.2,
    producerType = "Ölfeuerung",
    suppliedDistributionSystems = List("HE-1", "WW-1"),
    heatEmissionType = "Radiatoren",
    constructionYear = Some(1995),
    condition = Some("Mittel"),
    location = "Innerhalb thermischer Gebäudehülle",
    maintenanceCost = 800.0,
    investment = Some(35000.0),
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(20)
  )
  
  lazy val exampleHeatPump: HeatProducer = HeatProducer(
    code = "WE-2",
    description = "Luft-Wasser-Wärmepumpe",
    energySource = "Elektrizität (HT)",
    efficiencyHeating = 3.2,
    efficiencyHotWater = 2.8,
    oversizing = 1.0,
    producerType = "Wärmepumpe Luft/Wasser",
    suppliedDistributionSystems = List("HE-1", "WW-1"),
    heatEmissionType = "Flächenheizung",
    constructionYear = Some(2020),
    condition = Some("Gut"),
    location = "Ausserhalb thermischer Gebäudehülle",
    maintenanceCost = 400.0,
    investment = None,
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(20)
  )
  
  lazy val exampleSolar: HeatProducer = HeatProducer(
    code = "WE-3",
    description = "Solarthermie",
    energySource = "Solarenergie",
    efficiencyHeating = 0.0,
    efficiencyHotWater = 0.50,
    oversizing = 1.0,
    producerType = "Solarthermie",
    suppliedDistributionSystems = List("WW-1"),
    heatEmissionType = "Warmwasser",
    constructionYear = Some(2018),
    condition = Some("Gut"),
    location = "Dach",
    maintenanceCost = 200.0,
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(25)
  )
end HeatProducer

