package pme123.geak4s.domain

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.*
import pme123.geak4s.domain.project.*
import pme123.geak4s.domain.building.*
import pme123.geak4s.domain.envelope.*
import pme123.geak4s.domain.hvac.*
import pme123.geak4s.domain.energy.*
import pme123.geak4s.domain.uwert.*
import pme123.geak4s.domain.area.*

/**
 * Circe JSON codecs for all domain models using semiauto derivation
 * 
 * Import this object to get all encoders and decoders:
 * ```scala
 * import pme123.geak4s.domain.JsonCodecs.given
 * ```
 */
object JsonCodecs:
  
  // Enums
  given Encoder[Priority] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Priority] = Decoder.decodeString.map(Priority.valueOf)
  
  given Encoder[Condition] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Condition] = Decoder.decodeString.map(Condition.valueOf)
  
  given Encoder[Anrede] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Anrede] = Decoder.decodeString.map(Anrede.valueOf)
  
  // Project domain
  given Encoder[Address] = deriveEncoder[Address]
  given Decoder[Address] = deriveDecoder[Address]
  
  given Encoder[Client] = deriveEncoder[Client]
  given Decoder[Client] = deriveDecoder[Client]
  
  given Encoder[BuildingLocation] = deriveEncoder[BuildingLocation]
  given Decoder[BuildingLocation] = deriveDecoder[BuildingLocation]
  
  given Encoder[BuildingData] = deriveEncoder[BuildingData]
  given Decoder[BuildingData] = deriveDecoder[BuildingData]
  
  given Encoder[Descriptions] = deriveEncoder[Descriptions]
  given Decoder[Descriptions] = deriveDecoder[Descriptions]

  given Encoder[EgidEdidEntry] = deriveEncoder[EgidEdidEntry]
  given Decoder[EgidEdidEntry] = deriveDecoder[EgidEdidEntry]

  given Encoder[EgidEdidGroup] = deriveEncoder[EgidEdidGroup]
  given Decoder[EgidEdidGroup] = deriveDecoder[EgidEdidGroup]
  
  given Encoder[Project] = deriveEncoder[Project]
  given Decoder[Project] = deriveDecoder[Project]
  
  // Building domain
  given Encoder[BuildingUsage] = deriveEncoder[BuildingUsage]
  given Decoder[BuildingUsage] = deriveDecoder[BuildingUsage]
  
  // Envelope domain
  given Encoder[RoofCeiling] = deriveEncoder[RoofCeiling]
  given Decoder[RoofCeiling] = deriveDecoder[RoofCeiling]
  
  given Encoder[Wall] = deriveEncoder[Wall]
  given Decoder[Wall] = deriveDecoder[Wall]
  
  given Encoder[WindowDoor] = deriveEncoder[WindowDoor]
  given Decoder[WindowDoor] = deriveDecoder[WindowDoor]
  
  given Encoder[Floor] = deriveEncoder[Floor]
  given Decoder[Floor] = deriveDecoder[Floor]
  
  given Encoder[ThermalBridge] = deriveEncoder[ThermalBridge]
  given Decoder[ThermalBridge] = deriveDecoder[ThermalBridge]
  
  // HVAC domain
  given Encoder[HeatProducer] = deriveEncoder[HeatProducer]
  given Decoder[HeatProducer] = deriveDecoder[HeatProducer]
  
  given Encoder[HeatStorage] = deriveEncoder[HeatStorage]
  given Decoder[HeatStorage] = deriveDecoder[HeatStorage]
  
  given Encoder[HeatingDistribution] = deriveEncoder[HeatingDistribution]
  given Decoder[HeatingDistribution] = deriveDecoder[HeatingDistribution]
  
  given Encoder[HotWaterDistribution] = deriveEncoder[HotWaterDistribution]
  given Decoder[HotWaterDistribution] = deriveDecoder[HotWaterDistribution]
  
  given Encoder[Ventilation] = deriveEncoder[Ventilation]
  given Decoder[Ventilation] = deriveDecoder[Ventilation]
  
  // Energy domain
  given Encoder[ElectricityProducer] = deriveEncoder[ElectricityProducer]
  given Decoder[ElectricityProducer] = deriveDecoder[ElectricityProducer]
  
  // U-Wert calculations
  given Encoder[ComponentType] = Encoder.encodeString.contramap(_.toString)
  given Decoder[ComponentType] = Decoder.decodeString.map(ComponentType.valueOf)

  given Encoder[HeatTransfer] = deriveEncoder[HeatTransfer]
  given Decoder[HeatTransfer] = deriveDecoder[HeatTransfer]

  given Encoder[BuildingComponent] = deriveEncoder[BuildingComponent]
  given Decoder[BuildingComponent] = deriveDecoder[BuildingComponent]

  given Encoder[MaterialLayer] = deriveEncoder[MaterialLayer]
  given Decoder[MaterialLayer] = deriveDecoder[MaterialLayer]

  given Encoder[UWertTableData] = deriveEncoder[UWertTableData]
  given Decoder[UWertTableData] = deriveDecoder[UWertTableData]

  given Encoder[UWertCalculation] = deriveEncoder[UWertCalculation]
  given Decoder[UWertCalculation] = deriveDecoder[UWertCalculation]

  // Area calculations
  given Encoder[AreaEntry] = deriveEncoder[AreaEntry]
  given Decoder[AreaEntry] = deriveDecoder[AreaEntry]

  given Encoder[AreaCalculation] = deriveEncoder[AreaCalculation]
  given Decoder[AreaCalculation] = deriveDecoder[AreaCalculation]

  given Encoder[BuildingEnvelopeArea] = deriveEncoder[BuildingEnvelopeArea]
  given Decoder[BuildingEnvelopeArea] = deriveDecoder[BuildingEnvelopeArea]

  // Main project
  given Encoder[GeakProject] = deriveEncoder[GeakProject]
  given Decoder[GeakProject] = deriveDecoder[GeakProject]

end JsonCodecs

