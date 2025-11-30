package pme123.geak4s.domain.hvac

/** Versorgter Bereich Warmwasser - Hot Water Distribution */
case class HotWaterDistribution(
  code: String,
  description: String,
  area: Double,
  distributionType: String,
  heatProducerCoverage: Map[String, Double],
  distributionLinesInsulated: Boolean,
  insulationThickness: Option[Double],
  insulationConductivity: Option[Double],
  warmKeeping: String,
  horizontalLinesLocation: String,
  generalCondition: Option[String],
  priority: Option[String],
  possibleImprovements: Option[String],
  maintenanceCost: Double,
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object HotWaterDistribution:
  lazy val example: HotWaterDistribution = HotWaterDistribution(
    code = "WW-1",
    description = "Warmwasserverteilung Wohnungen",
    area = 850.5,
    distributionType = "zentral",
    heatProducerCoverage = Map("WE-1" -> 80.0, "WE-3" -> 20.0),
    distributionLinesInsulated = true,
    insulationThickness = Some(2.0),
    insulationConductivity = Some(0.035),
    warmKeeping = "Zirkulation",
    horizontalLinesLocation = "Innerhalb thermischer Gebäudehülle",
    generalCondition = Some("gebraucht"),
    priority = Some("Mittlere Priorität: Umsetzung in 2-5 Jahren"),
    possibleImprovements = Some("Bessere Dämmung Leitungen, Zirkulationspumpe optimieren"),
    maintenanceCost = 300.0,
    investment = Some(5000.0),
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30)
  )
  
  lazy val exampleDecentralized: HotWaterDistribution = HotWaterDistribution(
    code = "WW-2",
    description = "Warmwasserverteilung dezentral",
    area = 200.5,
    distributionType = "dezentral",
    heatProducerCoverage = Map("WE-2" -> 100.0),
    distributionLinesInsulated = true,
    insulationThickness = Some(3.0),
    insulationConductivity = Some(0.030),
    warmKeeping = "keine",
    horizontalLinesLocation = "Innerhalb thermischer Gebäudehülle",
    generalCondition = Some("neuwertig"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None,
    maintenanceCost = 100.0,
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30)
  )
end HotWaterDistribution

