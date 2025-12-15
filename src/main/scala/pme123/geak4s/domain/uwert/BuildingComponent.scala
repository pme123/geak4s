package pme123.geak4s.domain.uwert

case class BuildingComponent(
    label: String,
    heatTransferFromInside: HeatTransfer,
    heatTransferToOutside: HeatTransfer,
    materials: Seq[HeatTransfer] = Seq.empty
)

case class HeatTransfer(
    label: String,
    thicknessInM: Double,
    lambda: Double
)

lazy val buildingComponents: Seq[BuildingComponent] =
  Seq(
    BuildingComponent(
      label = "Kellerboden",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToGround
    ),
    BuildingComponent(
      label = "Kellerwand gg. Aussen",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Kellerwand gg. Erdreich",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToGround
    ),
    BuildingComponent(
      label = "Kellerwand gg. Unbeheizt",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      label = "Keller-Decke",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      label = "Aussenwand",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Boden gg. Aussen",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Storenabdeckung mit Aerogel",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    ),
    BuildingComponent(
      label = "Estrichboden",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideUnheated
    ),
    BuildingComponent(
      label = "Steildach",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutsideVentilated
    ),
    BuildingComponent(
      label = "Flachdach",
      heatTransferFromInside = transferFromInside,
      heatTransferToOutside = transferToOutside
    )
  )

// Heat transfer definitions
lazy val transferFromInside           = HeatTransfer("Wärmeübergang Innen", 1, 8)
lazy val transferToOutside            = HeatTransfer("Wärmeübergang gegen aussen", 1, 25)
lazy val transferToOutsideUnheated    = HeatTransfer("Wärmeübergang gegen Unbeheizt", 1, 8)
lazy val transferToGround             = HeatTransfer("Wärmeübergang gegen Erdreich", 1, 0)  // No heat transfer coefficient for ground
lazy val transferToOutsideVentilated  = HeatTransfer("äusserer Übergang bei Hinterlüftung", 1, 12.5)
