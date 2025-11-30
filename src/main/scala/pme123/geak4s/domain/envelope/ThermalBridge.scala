package pme123.geak4s.domain.envelope

/** Wärmebrücken - Thermal Bridges */
case class ThermalBridge(
  code: String,
  description: String,
  bridgeType: String,
  renovationYear: Option[Int],
  length: Option[Double],
  psiValue: Option[Double],
  chiValue: Option[Double],
  quantity: Int,
  bFactor: Double,
  priority: Option[String],
  possibleImprovements: Option[String]
)

object ThermalBridge:
  lazy val example: ThermalBridge = ThermalBridge(
    code = "WL-1",
    description = "Balkonanschluss ungedämmt",
    bridgeType = "Balkon",
    renovationYear = None,
    length = Some(24.0),
    psiValue = Some(0.8),
    chiValue = None,
    quantity = 6,
    bFactor = 1.0,
    priority = Some("Mittlere Priorität: Umsetzung in 2-5 Jahren"),
    possibleImprovements = Some("Thermische Trennung")
  )
  
  lazy val examplePoint: ThermalBridge = ThermalBridge(
    code = "WL-2",
    description = "Stütze durchdringend",
    bridgeType = "Gebäudesockel",
    renovationYear = None,
    length = None,
    psiValue = None,
    chiValue = Some(1.2),
    quantity = 4,
    bFactor = 1.0,
    priority = Some("Geringe Priorität: Umsetzung in 5-10 Jahren"),
    possibleImprovements = Some("Dämmung Sockelbereich")
  )
end ThermalBridge

