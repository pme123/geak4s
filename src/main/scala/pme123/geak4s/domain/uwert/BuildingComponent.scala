package pme123.geak4s.domain.uwert

/** Building component type classification */
enum ComponentType:
  case BasementFloor        // Kellerboden
  case BasementWall         // Kellerwand
  case BasementCeiling      // Keller-Decke
  case ExteriorWall         // Aussenwand
  case Floor                // Boden
  case AtticFloor           // Estrichboden
  case PitchedRoof          // Steildach
  case FlatRoof             // Flachdach
  case Other                // Sonstige (e.g., Storenabdeckung)

case class BuildingComponent(
    label: String,
    compType: ComponentType,
    heatTransferFromInside: HeatTransfer,
    heatTransferToOutside: HeatTransfer,
    materials: Seq[HeatTransfer] = Seq.empty
)

case class HeatTransfer(
    label: String,
    thicknessInM: Double,
    // Thermal conductivity lambda in W/(m·K)
    thermalConductivity: Double
)

lazy val buildingComponents: Seq[BuildingComponent] =
  Seq(
    BuildingComponent(
      label = "Kellerboden",
      compType = ComponentType.BasementFloor,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToGround
    ),
    BuildingComponent(
      label = "Kellerwand gg. Aussen",
      compType = ComponentType.BasementWall,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Kellerwand gg. Erdreich",
      compType = ComponentType.BasementWall,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToGround
    ),
    BuildingComponent(
      label = "Kellerwand gg. Unbeheizt",
      compType = ComponentType.BasementWall,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      label = "Keller-Decke",
      compType = ComponentType.BasementCeiling,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      label = "Aussenwand",
      compType = ComponentType.ExteriorWall,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Boden gg. Aussen",
      compType = ComponentType.Floor,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Storenabdeckung mit Aerogel",
      compType = ComponentType.Other,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Estrichboden",
      compType = ComponentType.AtticFloor,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      label = "Steildach",
      compType = ComponentType.PitchedRoof,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideVentilated
    ),
    BuildingComponent(
      label = "Flachdach",
      compType = ComponentType.FlatRoof,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    )
  )

// Heat transfer definitions
lazy val transferFromInside        = HeatTransfer("Wärmeübergang Innen", 1, 8)
lazy val transferToOutside         = HeatTransfer("Wärmeübergang gegen aussen", 1, 25)
lazy val transferToOutsideUnheated = HeatTransfer("Wärmeübergang gegen Unbeheizt", 1, 8)
lazy val transferToGround          =
  HeatTransfer("Wärmeübergang gegen Erdreich", 1, 0) // No heat transfer coefficient for ground
lazy val transferToOutsideVentilated = HeatTransfer("äusserer Übergang bei Hinterlüftung", 1, 12.5)
