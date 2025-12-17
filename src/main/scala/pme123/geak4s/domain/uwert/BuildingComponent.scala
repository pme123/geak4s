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

  /**
   * Get background color for this component type
   * Returns a light pastel color for visual distinction
   */
  def color: String = this match
    case EBF                    => "#fff9c4" // Light yellow
    case BasementFloor          => "#e3f2fd" // Light blue
    case BasementWallToOutside  => "#f3e5f5" // Light purple
    case BasementWallToEarth    => "#e8f5e9" // Light green
    case BasementWallToUnheated => "#fff3e0" // Light orange
    case BasementCeiling        => "#fce4ec" // Light pink
    case ExteriorWall           => "#e0f2f1" // Light teal
    case FloorToOutside         => "#f1f8e9" // Light lime
    case AtticFloor             => "#ede7f6" // Light deep purple
    case PitchedRoof            => "#e1f5fe" // Light cyan
    case FlatRoof               => "#f3e5f5" // Light purple (alternate)
    case ShutterBoxCover        => "#fff3e0" // Light orange (alternate)

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
