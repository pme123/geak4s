# GEAK Domain Model

This package contains all domain objects for the GEAK (Gebäudeenergieausweis der Kantone) system, based on the Excel template structure.

## Package Structure

The domain model is organized into sub-packages that mirror the Excel sheet structure:

```
pme123.geak4s.domain/
├── project/          # Project information
│   ├── Project.scala
│   ├── Client.scala
│   ├── Expert.scala
│   ├── EgidEdidGroup.scala
│   ├── BuildingLocation.scala
│   └── BuildingData.scala
├── building/         # Building usage
│   └── BuildingUsage.scala
├── envelope/         # Building envelope
│   ├── RoofCeiling.scala
│   ├── Wall.scala
│   ├── WindowDoor.scala
│   ├── Floor.scala
│   └── ThermalBridge.scala
├── hvac/            # HVAC systems
│   ├── HeatProducer.scala
│   ├── HeatStorage.scala
│   ├── HeatingDistribution.scala
│   ├── HotWaterDistribution.scala
│   └── Ventilation.scala
├── energy/          # Energy production
│   └── ElectricityProducer.scala
├── JSHelpers.scala  # JavaScript interop utilities
└── package.scala    # Common types and GeakProject container
```

## Overview

### 1. Project Information (`pme123.geak4s.domain.project`)
- **Project**: Main project container with all metadata
- **Client**: Client/customer information (Auftraggeber)
- **Expert**: GEAK expert information
- **EgidEdidGroup**: Building identification (EGID/EDID)
- **BuildingLocation**: Geographic location and weather data
- **BuildingData**: Basic building characteristics

### 2. Building Usage (`pme123.geak4s.domain.building`)
- **BuildingUsage**: Usage types and areas (Gebäudenutzungen)

### 3. Building Envelope (`pme123.geak4s.domain.envelope`)
- **RoofCeiling**: Roofs and ceilings (Dächer und Decken)
- **Wall**: Walls (Wände)
- **WindowDoor**: Windows and doors (Fenster und Türen)
- **Floor**: Floors (Böden)
- **ThermalBridge**: Thermal bridges (Wärmebrücken)

### 4. HVAC Systems (`pme123.geak4s.domain.hvac`)
- **HeatProducer**: Heat generators (Wärmeerzeuger)
- **HeatStorage**: Heat storage tanks (Speicher)
- **HeatingDistribution**: Heating distribution (Versorgter Bereich Heizung)
- **HotWaterDistribution**: Hot water distribution (Versorgter Bereich Warmwasser)
- **Ventilation**: Ventilation systems (Lüftung)

### 5. Energy Production (`pme123.geak4s.domain.energy`)
- **ElectricityProducer**: PV systems, CHP units (Elektrizitätsprod.)

## Usage

### Imports

You can import from specific sub-packages or use the wildcard import:

```scala
// Import specific packages
import pme123.geak4s.domain.project.*
import pme123.geak4s.domain.envelope.*
import pme123.geak4s.domain.hvac.*

// Or import everything
import pme123.geak4s.domain.*
```

### Example Data

Each domain object has a companion object with example data:

```scala
import pme123.geak4s.domain.*

// Single examples
val project = Project.example
val wall = Wall.example
val heatPump = HeatProducer.exampleHeatPump

// Complete project
val completeProject = GeakProject.example
```

### Creating Custom Data

```scala
import pme123.geak4s.domain.*

val myWall = Wall(
  code = "W-1",
  description = "Aussenwand Süd",
  wallType = "Aussenwand",
  orientation = Some("S"),
  renovationYear = Some(2020),
  area = 150.0,
  uValue = 0.20,
  bFactor = 1.0,
  quantity = 1,
  wallHeating = false,
  neighborRoomTemp = None,
  generalCondition = Some("neuwertig"),
  priority = Some("Keine Priorität"),
  possibleImprovements = None,
  maintenanceCost = Some(300.0),
  investment = None,
  calculationBase = Some("Pro m²"),
  usefulLife = Some(40)
)
```

### Complete Project Structure

```scala
import pme123.geak4s.domain.*

val geakProject = GeakProject(
  project = Project.example,
  buildingUsages = List(BuildingUsage.example),
  roofsCeilings = List(RoofCeiling.example),
  walls = List(Wall.example, Wall.exampleRenovated),
  windowsDoors = List(WindowDoor.example),
  floors = List(Floor.example),
  thermalBridges = List(ThermalBridge.example),
  heatProducers = List(HeatProducer.exampleHeatPump),
  heatStorages = List(HeatStorage.example),
  heatingDistributions = List(HeatingDistribution.example),
  hotWaterDistributions = List(HotWaterDistribution.example),
  ventilations = List(Ventilation.example),
  electricityProducers = List(ElectricityProducer.examplePV)
)
```

## JavaScript Interop

The `JSHelpers` object provides utilities for reading data from Excel via JavaScript:

```scala
import pme123.geak4s.domain.JSHelpers.*
import scala.scalajs.js

def parseWall(row: js.Dynamic): Wall =
  Wall(
    code = row.getString("Kürzel"),
    description = row.getString("Bezeichnung"),
    wallType = row.getString("Typ"),
    orientation = row.getStringOpt("Ausrichtung"),
    renovationYear = row.getIntOpt("Renovationsjahr"),
    area = row.getDouble("Fläche [m²]"),
    uValue = row.getDouble("U-Wert [W/(m²K)]"),
    bFactor = row.getDouble("b-Faktor [—]", 1.0),
    quantity = row.getInt("Anzahl [—]", 1),
    wallHeating = row.getBoolean("Bauteilheizung"),
    neighborRoomTemp = row.getDoubleOpt("Temp. Nachbarraum"),
    generalCondition = row.getStringOpt("Allgemeiner Zustand"),
    priority = row.getStringOpt("Priorität"),
    possibleImprovements = row.getStringOpt("Mögliche Verbesserungen"),
    maintenanceCost = row.getDoubleOpt("Instandhaltungskosten"),
    investment = row.getDoubleOpt("Investition"),
    calculationBase = row.getStringOpt("Berechnungsgrundlage"),
    usefulLife = row.getIntOpt("Nutzungsdauer [Jahre]")
  )
```

## Enums

The package provides several enums for common values:

### Priority
```scala
enum Priority:
  case High      // Hohe Priorität: Umsetzung in < 2 Jahren
  case Medium    // Mittlere Priorität: Umsetzung in 2-5 Jahren
  case Low       // Geringe Priorität: Umsetzung in 5-10 Jahren
  case None      // Keine Priorität

Priority.fromString("Hohe Priorität: Umsetzung in < 2 Jahren") // Priority.High
```

### Condition
```scala
enum Condition:
  case New          // neuwertig
  case Good         // gebraucht
  case Worn         // abgenutzt
  case EndOfLife    // Lebensdauer erreicht
  case Unknown

Condition.fromString("neuwertig") // Condition.New
```

### Orientation
```scala
enum Orientation:
  case N, NO, O, SO, S, SW, W, NW
  case Horizontal
  case Unknown

Orientation.fromString("S") // Orientation.S
```

## Field Descriptions

### Common Fields

Most building components share these common fields:

- **code**: Unique identifier (e.g., "W-1", "Da-1")
- **description**: Human-readable description
- **renovationYear**: Year of last renovation
- **area**: Area in m²
- **uValue**: U-value in W/(m²K) - thermal transmittance
- **bFactor**: Temperature reduction factor
- **quantity**: Number of identical components
- **generalCondition**: Current condition (neuwertig, gebraucht, abgenutzt)
- **priority**: Renovation priority
- **possibleImprovements**: Suggested improvements
- **maintenanceCost**: Annual maintenance cost in CHF
- **investment**: Investment cost in CHF
- **calculationBase**: Basis for calculations (Pro m², Pauschal, etc.)
- **usefulLife**: Expected useful life in years

### Energy-Specific Fields

- **efficiencyHeating**: Heating efficiency (0-1 or COP for heat pumps)
- **efficiencyHotWater**: Hot water efficiency
- **energySource**: Energy source (Heizöl, Elektrizität, Gas, etc.)
- **annualProduction**: Annual energy production in kWh
- **gridFeedIn**: Percentage fed into grid

## Excel Mapping

The domain model maps directly to the GEAK Excel template sheets:

| Excel Sheet | Domain Object |
|-------------|---------------|
| Projekt | Project, Client, Expert, EgidEdidGroup, BuildingLocation, BuildingData |
| Gebäudenutzungen | BuildingUsage |
| Dächer und Decken | RoofCeiling |
| Wände | Wall |
| Fenster und Türen | WindowDoor |
| Böden | Floor |
| Wärmebrücken | ThermalBridge |
| Wärmeerzeuger | HeatProducer |
| Speicher | HeatStorage |
| Versorgter Bereich Heizung | HeatingDistribution |
| Versorgter Bereich Warmwasser | HotWaterDistribution |
| Lüftung | Ventilation |
| Elektrizitätsprod. | ElectricityProducer |

## Future Extensions

Potential additions to the domain model:

- Consumption data (Verbrauchsdaten)
- Electrical devices (Geräte und Installationen)
- Lighting (Beleuchtung)
- Small appliances (Kleingeräte und Elektronik)
- Operating equipment (Betriebseinrichtungen)
- Other consumers (Weitere Verbraucher)
- Energy management systems (Energiemanagement)
- Battery storage (Speicherbatterie)

## Contributing

When adding new domain objects:

1. Create a separate file for each case class
2. Add a companion object with `example` data
3. Include multiple examples if useful (e.g., `exampleNew`, `exampleOld`)
4. Document all fields with comments
5. Update this README with the new object
6. Add the object to `GeakProject` if it's a main component

## License

This domain model is based on the official GEAK template structure.

