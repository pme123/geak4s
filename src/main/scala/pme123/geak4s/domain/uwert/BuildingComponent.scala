package pme123.geak4s.domain.uwert

/** Building component type classification */
enum ComponentType:
  case EBF                    // Energiebezugsfläche
  case BasementFloor          // Kellerboden
  case BasementWallToOutside  // Kellerwand
  case BasementWallToEarth    // Kellerwand
  case BasementWallToUnheated // Kellerwand
  case BasementCeiling        // Keller-Decke
  case ExteriorWall           // Aussenwand
  case FloorToOutside         // Boden
  case AtticFloor             // Estrichboden
  case PitchedRoof            // Steildach
  case FlatRoof               // Flachdach
  case ShutterBoxCover

  def label: String = this match
  case EBF                    => "EBF - Energiebezugsfläche"
  case BasementFloor          => "Kellerboden"
  case BasementWallToOutside  => "Kellerwand gg. Aussen"
  case BasementWallToEarth    => "Kellerwand gg. Erdreich"
  case BasementWallToUnheated => "Kellerwand gg. Unbeheizt"
  case BasementCeiling        => "Keller-Decke"
  case ExteriorWall           => "Aussenwand"
  case FloorToOutside         => "Boden gg. Aussen"
  case AtticFloor             => "Estrichboden"
  case PitchedRoof            => "Steildach"
  case FlatRoof               => "Flachdach"
  case ShutterBoxCover        => "Storenabdeckung mit Aerogel"

end ComponentType

case class BuildingComponent(
    compType: ComponentType,
    heatTransferFromInside: HeatTransfer,
    heatTransferToOutside: HeatTransfer,
    materials: Seq[HeatTransfer] = Seq.empty
):
  lazy val label: String = compType.label
end BuildingComponent

case class HeatTransfer(
    label: String,
    thicknessInM: Double,
    // Thermal conductivity lambda in W/(m·K)
    thermalConductivity: Double
)

lazy val buildingComponents: Seq[BuildingComponent] =
  Seq(
    BuildingComponent(
      compType = ComponentType.BasementFloor,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToGround
    ),
    BuildingComponent(
      compType = ComponentType.BasementWallToOutside,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      compType = ComponentType.BasementWallToEarth,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToGround
    ),
    BuildingComponent(
      compType = ComponentType.BasementWallToUnheated,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      compType = ComponentType.BasementCeiling,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      compType = ComponentType.ExteriorWall,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      compType = ComponentType.FloorToOutside,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      compType = ComponentType.ShutterBoxCover,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      compType = ComponentType.AtticFloor,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      compType = ComponentType.PitchedRoof,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideVentilated
    ),
    BuildingComponent(
      compType = ComponentType.FlatRoof,
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    )
  )

// Heat transfer definitions
lazy val transferFromInside          = HeatTransfer("Wärmeübergang Innen", 1, 8)
lazy val transferToOutside           = HeatTransfer("Wärmeübergang gegen aussen", 1, 25)
lazy val transferToOutsideUnheated   = HeatTransfer("Wärmeübergang gegen Unbeheizt", 1, 8)
lazy val transferToGround            =
  HeatTransfer("Wärmeübergang gegen Erdreich", 1, 0) // No heat transfer coefficient for ground
lazy val transferToOutsideVentilated = HeatTransfer("äusserer Übergang bei Hinterlüftung", 1, 12.5)
