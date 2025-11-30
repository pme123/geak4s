package pme123.geak4s.domain.hvac

/** Versorgter Bereich Heizung - Heating Distribution */
case class HeatingDistribution(
  code: String,
  description: String,
  area: Double,
  distributionType: String,
  heatEmissionType: String,
  heatProducerCoverage: Map[String, Double],
  mainHeatProducer: String,
  distributionLinesInsulated: Boolean,
  insulationThickness: Option[Double],
  insulationConductivity: Option[Double],
  flowReturnTemp: String,
  horizontalLinesLocation: String,
  hydraulicBalancing: Boolean,
  generalCondition: Option[String],
  priority: Option[String],
  possibleImprovements: Option[String],
  maintenanceCost: Double,
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object HeatingDistribution:
  lazy val example: HeatingDistribution = HeatingDistribution(
    code = "HE-1",
    description = "Heizverteilung Wohnungen",
    area = 850.5,
    distributionType = "zentral",
    heatEmissionType = "Radiatoren",
    heatProducerCoverage = Map("WE-1" -> 100.0),
    mainHeatProducer = "WE-1",
    distributionLinesInsulated = true,
    insulationThickness = Some(2.0),
    insulationConductivity = Some(0.035),
    flowReturnTemp = "70/55",
    horizontalLinesLocation = "Innerhalb thermischer Gebäudehülle",
    hydraulicBalancing = false,
    generalCondition = Some("gebraucht"),
    priority = Some("Mittlere Priorität: Umsetzung in 2-5 Jahren"),
    possibleImprovements = Some("Hydraulischer Abgleich, Absenkung Vorlauftemperatur"),
    maintenanceCost = 500.0,
    investment = Some(8000.0),
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30)
  )
  
  lazy val exampleModern: HeatingDistribution = HeatingDistribution(
    code = "HE-2",
    description = "Heizverteilung Büros",
    area = 200.5,
    distributionType = "zentral",
    heatEmissionType = "Flächenheizung",
    heatProducerCoverage = Map("WE-2" -> 100.0),
    mainHeatProducer = "WE-2",
    distributionLinesInsulated = true,
    insulationThickness = Some(4.0),
    insulationConductivity = Some(0.030),
    flowReturnTemp = "35/28",
    horizontalLinesLocation = "Innerhalb thermischer Gebäudehülle",
    hydraulicBalancing = true,
    generalCondition = Some("neuwertig"),
    priority = Some("Keine Priorität"),
    possibleImprovements = None,
    maintenanceCost = 200.0,
    investment = None,
    calculationBase = Some("Pro m²"),
    usefulLife = Some(30)
  )
end HeatingDistribution

