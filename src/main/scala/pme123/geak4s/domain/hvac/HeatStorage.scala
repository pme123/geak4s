package pme123.geak4s.domain.hvac

/** Speicher - Heat Storage */
case class HeatStorage(
  code: String,
  description: String,
  storageType: String,
  totalVolume: Double,
  hotWaterVolume: Double,
  heatingVolume: Double,
  location: String,
  connectionQuality: String,
  heightToDiameterRatio: Double,
  maintenanceCost: Double,
  connectedHeatProducers: List[String],
  investment: Option[Double],
  calculationBase: Option[String],
  usefulLife: Option[Int]
)

object HeatStorage:
  lazy val example: HeatStorage = HeatStorage(
    code = "SP-1",
    description = "Warmwasserspeicher 300L",
    storageType = "Warmwasserspeicher",
    totalVolume = 300.0,
    hotWaterVolume = 300.0,
    heatingVolume = 0.0,
    location = "Innerhalb thermischer Gebäudehülle",
    connectionQuality = "Gut",
    heightToDiameterRatio = 3.0,
    maintenanceCost = 150.0,
    connectedHeatProducers = List("WE-1"),
    investment = Some(3500.0),
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(20)
  )
  
  lazy val exampleCombi: HeatStorage = HeatStorage(
    code = "SP-2",
    description = "Kombispeicher 800L",
    storageType = "Kombispeicher",
    totalVolume = 800.0,
    hotWaterVolume = 200.0,
    heatingVolume = 600.0,
    location = "Innerhalb thermischer Gebäudehülle",
    connectionQuality = "Sehr gut",
    heightToDiameterRatio = 4.0,
    maintenanceCost = 200.0,
    connectedHeatProducers = List("WE-2", "WE-3"),
    investment = None,
    calculationBase = Some("Pauschal (pro Stück)"),
    usefulLife = Some(20)
  )
end HeatStorage

