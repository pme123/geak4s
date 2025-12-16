package pme123.geak4s.domain.uwert

/** Building material with thermal conductivity and applicable component types */
case class BuildingMaterial(
    name: String,
    thermalConductivity: Double,                  // λ [W/(m·K)] - Lambda value
    applicableFor: Set[ComponentType] = Set.empty // Component types this material can be used for
)

/** Catalog of building components with their thermal conductivity values */
object BuildingComponentCatalog:

  import ComponentType.*

  /** All available building components */
  val components: List[BuildingMaterial] = List(
    // Aerogel and high-performance insulation
    BuildingMaterial(
      "Aerogel, Agitech Spaceloft",
      0.015,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor, Other)
    ),

    // Masonry materials
    BuildingMaterial("Ziegel", 0.8, Set(ExteriorWall, BasementWall)),
    BuildingMaterial("Bollenstein, ab ca. 50 cm dick", 0.9, Set(ExteriorWall, BasementWall)),
    BuildingMaterial("Backstein (Einsteinmauerwerk)", 0.44, Set(ExteriorWall, BasementWall)),
    BuildingMaterial("Backstein (Verbandmrwk) d=28-30", 0.37, Set(ExteriorWall, BasementWall)),
    BuildingMaterial("Kalksandstein", 0.8, Set(ExteriorWall, BasementWall)),

    // Concrete materials
    BuildingMaterial(
      "Stahlbeton, 1% Stahl / Steinbodenplatten",
      2.3,
      Set(BasementFloor, BasementWall, BasementCeiling, Floor, ExteriorWall)
    ),

    // Natural materials
    BuildingMaterial("Schilf (alte Gebäude)", 0.065, Set(PitchedRoof, ExteriorWall, AtticFloor)),
    BuildingMaterial("Holz", 0.14, Set(ExteriorWall, PitchedRoof, Floor, AtticFloor)),

    // Plaster
    BuildingMaterial("Aussenputz", 0.87, Set(ExteriorWall, BasementWall)),
    BuildingMaterial(
      "Innenputz",
      0.7,
      Set(ExteriorWall, BasementWall, BasementCeiling, AtticFloor)
    ),

    // Composite elements (Note: These have U-values, not lambda values)
    // BuildingMaterial("Hourdisdecke komlett (gemäss Abbildung)", 0.9),  // This is a U-value
    // BuildingMaterial("Holzbalkendecke komplett (gemäss Abbildung)", 0.8),  // This is a U-value
    BuildingMaterial("Tonhourdis (Kellerdecke)", 0.44, Set(BasementCeiling, Floor)),

    // Other materials
    BuildingMaterial("Unterlagsboden", 1.4, Set(BasementFloor, Floor, AtticFloor)),
    BuildingMaterial("Gipskartonplatte", 0.21, Set(ExteriorWall, AtticFloor, BasementCeiling)),
    BuildingMaterial(
      "Heraklith, Holzwolle (HWL) Zementgebunden, Perfecta",
      0.095,
      Set(PitchedRoof, ExteriorWall, AtticFloor, BasementCeiling)
    ),

    // Insulation materials (Dämm-Material)
    BuildingMaterial(
      "Kork/Korkschrottmatte",
      0.056,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial(
      "Wärmedämmung, Prod. Unbekannt (bspw. Styropor bzw. EPS)",
      0.038,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor, BasementFloor)
    ),
    BuildingMaterial("Schlacke", 0.35, Set(BasementFloor, AtticFloor)),

    // Exterior wall insulation (Aussenwand)
    BuildingMaterial("Flumroc COMPACT PRO, Fassade", 0.033, Set(ExteriorWall)),
    BuildingMaterial(
      "Swisspor Lamda White 031, EPS",
      0.031,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial("Multipor Innendämmung", 0.042, Set(ExteriorWall, BasementWall)),
    BuildingMaterial(
      "SwissporXPS Premium Plus 300",
      0.027,
      Set(ExteriorWall, BasementFloor, BasementWall, FlatRoof)
    ),
    BuildingMaterial("Foamglas T4+", 0.041, Set(ExteriorWall, BasementWall, FlatRoof)),

    // Roof insulation (Dach)
    BuildingMaterial(
      "Flumroc-Dämmplatte 3, 15% Holzanteil (innen wenig Sparren)",
      0.039,
      Set(PitchedRoof, AtticFloor, BasementCeiling)
    ),
    BuildingMaterial("Pavatherm-Plus, Dämmung+Unterdach", 0.043, Set(PitchedRoof)),
    BuildingMaterial(
      "Isofloc oder bestehend, 15% Holzanteil (Standard)",
      0.045,
      Set(PitchedRoof, AtticFloor)
    ),
    BuildingMaterial("SwissporLAMDA Roof", 0.029, Set(PitchedRoof, FlatRoof)),
    BuildingMaterial(
      "PU-Dämm. für Flachdach/Terrasse/Boden, Vlies",
      0.026,
      Set(FlatRoof, BasementFloor, Floor)
    ),

    // Basement ceiling insulation (UG-Decke)
    BuildingMaterial("Thermo-Plus, Glaswolle, Kellerdecke", 0.031, Set(BasementCeiling)),
    // Note: "Flumroc-Dämmplatte 3, 15% Holzanteil (innen wenig Sparren)" already added above

    // Interior wall insulation (Innenwand)
    BuildingMaterial(
      "WILAN 3, mit einseitiger Fertigdeckschicht",
      0.031,
      Set(ExteriorWall, BasementWall)
    ),
    // Note: "Swisspor Lamda White 031, EPS" already added above
    BuildingMaterial("Foamglas T3+", 0.036, Set(ExteriorWall, BasementWall)),

    // Attic floor insulation (DG-Boden)
    // Note: "Flumroc-Dämmplatte 3 m. 15% Holzanteil" already added above
    BuildingMaterial("Flumroc ESTRA, Estrichbodenplatte", 0.034, Set(AtticFloor, Floor)),

    // German material names (Deutsche Materialnamen)
    // Note: Many materials below are German equivalents of English names already in the catalog

    // Wood materials (Holz)
    BuildingMaterial("Buchenholz", 0.17, Set(ExteriorWall, PitchedRoof, Floor, AtticFloor)),
    BuildingMaterial("Eichenholz", 0.21, Set(ExteriorWall, PitchedRoof, Floor, AtticFloor)),
    BuildingMaterial("Fichtenholz", 0.14, Set(ExteriorWall, PitchedRoof, Floor, AtticFloor)),

    // Panels and boards (Platten)
    BuildingMaterial("Duripanelplatte", 0.26, Set(ExteriorWall, PitchedRoof, AtticFloor)),
    BuildingMaterial("Durisol-Dachplatte", 0.12, Set(PitchedRoof, FlatRoof)),
    BuildingMaterial("Gipsplatte", 0.4, Set(ExteriorWall, AtticFloor, BasementCeiling)),
    BuildingMaterial("Holzspanplatte", 0.12, Set(ExteriorWall, PitchedRoof, AtticFloor)),
    BuildingMaterial("Holzspanplatte halbhart", 0.085, Set(ExteriorWall, PitchedRoof, AtticFloor)),
    BuildingMaterial("Holzspanplatte hart", 0.17, Set(ExteriorWall, Floor)),
    BuildingMaterial("Holzspanplatte Novophen", 0.12, Set(ExteriorWall, PitchedRoof, AtticFloor)),
    BuildingMaterial("Holzspanplatte weich", 0.06, Set(PitchedRoof, AtticFloor)),
    BuildingMaterial("Keramische Platten", 1.0, Set(Floor, BasementFloor)),
    BuildingMaterial("Klinkerplatten", 1.0, Set(ExteriorWall, Floor)),
    BuildingMaterial("Marmorplatten", 2.3, Set(Floor, BasementFloor)),
    BuildingMaterial("Steinzeugplatten", 1.5, Set(Floor, BasementFloor)),
    BuildingMaterial("Tonplatten", 1.0, Set(Floor, BasementFloor, PitchedRoof)),
    BuildingMaterial(
      "Holzwollenplatte (Bspw. Gargendecke)",
      0.09,
      Set(BasementCeiling, AtticFloor, PitchedRoof)
    ),

    // Insulation materials (Dämmmaterialien)
    BuildingMaterial("Flumroc Dachplatte", 0.04, Set(PitchedRoof, FlatRoof, AtticFloor)),
    BuildingMaterial(
      "Flumroc Isolierplatte",
      0.036,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial("Flumroc-Feingranulat", 0.04, Set(AtticFloor, BasementCeiling)),
    BuildingMaterial(
      "Foamglasplatte",
      0.044,
      Set(ExteriorWall, BasementWall, FlatRoof, BasementFloor)
    ),
    BuildingMaterial("Gasbeton", 0.16, Set(ExteriorWall, BasementWall)),
    BuildingMaterial("Gasbeton", 0.19, Set(ExteriorWall, BasementWall)),
    BuildingMaterial("Gasbeton-Dachplatte", 0.2, Set(PitchedRoof, FlatRoof)),
    BuildingMaterial(
      "Glasfasern",
      0.04,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial(
      "Glasfaserplatte",
      0.04,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial(
      "Glaswolle",
      0.04,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial("Kokosfasermatte", 0.05, Set(ExteriorWall, PitchedRoof, AtticFloor)),
    BuildingMaterial(
      "Korkplatte",
      0.042,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial(
      "Korkschrot expandiert",
      0.042,
      Set(ExteriorWall, PitchedRoof, FlatRoof, AtticFloor)
    ),
    BuildingMaterial("Korkschrot natur", 0.06, Set(AtticFloor, BasementCeiling)),
    BuildingMaterial("Korkschrotmatte", 0.046, Set(ExteriorWall, PitchedRoof, AtticFloor)),
    BuildingMaterial("Perlit-Platte", 0.06, Set(ExteriorWall, PitchedRoof, FlatRoof, AtticFloor)),
    BuildingMaterial("Perlit-Schüttung", 0.07, Set(AtticFloor, BasementCeiling)),
    BuildingMaterial(
      "Polystyrolplatte exp.",
      0.038,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor, BasementFloor)
    ),
    BuildingMaterial(
      "Polystyrolplatte extr.",
      0.034,
      Set(ExteriorWall, BasementWall, FlatRoof, BasementFloor)
    ),
    BuildingMaterial(
      "Polyurehanhartschaum",
      0.03,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, BasementFloor)
    ),
    BuildingMaterial("Sarna-Aussendämmpl.", 0.038, Set(ExteriorWall, FlatRoof)),
    BuildingMaterial("Sarnapur-Platte", 0.03, Set(FlatRoof, BasementFloor)),
    BuildingMaterial("Sarnatherm-Polystyrolpl.", 0.036, Set(ExteriorWall, FlatRoof)),
    BuildingMaterial(
      "Schaumglasolatte",
      0.048,
      Set(ExteriorWall, BasementWall, FlatRoof, BasementFloor)
    ),
    BuildingMaterial(
      "Schaumpolystyrol exp.",
      0.038,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial(
      "Steinwolle",
      0.04,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial(
      "Steinwollplatte",
      0.04,
      Set(ExteriorWall, PitchedRoof, FlatRoof, BasementCeiling, AtticFloor)
    ),
    BuildingMaterial("Tonisolierplatte", 0.44, Set(BasementCeiling, Floor)),
    BuildingMaterial(
      "Zementgeb. Holzwollepl.",
      0.11,
      Set(PitchedRoof, ExteriorWall, AtticFloor, BasementCeiling)
    ),

    // Other materials (Sonstige)
    BuildingMaterial("Gussasphaltbelag", 0.7, Set(Floor, FlatRoof)),
    BuildingMaterial("Kies/Sand-Schutzschicht", 1.8, Set(FlatRoof, BasementFloor)),
    BuildingMaterial("Rundkies", 2.3, Set(BasementFloor, Floor)),
    BuildingMaterial("Splitt", 1.5, Set(BasementFloor, Floor)),
    BuildingMaterial("Zementüberzug", 1.5, Set(Floor, BasementFloor, FlatRoof))
  )

  /** Get thermal conductivity by component name */
  def getThermalConductivityByName(name: String): Option[Double] =
    components.find(_.name.equalsIgnoreCase(name)).map(_.thermalConductivity)

  /** Get all component names for autocomplete */
  def getAllNames: List[String] =
    components.map(_.name).distinct.sorted

  /** Search components by partial name match */
  def searchByName(partial: String): List[BuildingMaterial] =
    if partial.isEmpty then List.empty
    else components.filter(_.name.toLowerCase.contains(partial.toLowerCase)).distinct

  /** Get components grouped by thermal conductivity range */
  def getByThermalConductivityRange(min: Double, max: Double): List[BuildingMaterial] =
    components.filter(c => c.thermalConductivity >= min && c.thermalConductivity <= max)

  /** Get all unique thermal conductivity values */
  def getAllThermalConductivities: List[Double] =
    components.map(_.thermalConductivity).distinct.sorted

  /** Get materials applicable for a specific component type */
  def getByComponentType(componentType: ComponentType): List[BuildingMaterial] =
    components.filter(_.applicableFor.contains(componentType)).distinct.sortBy(_.name)

end BuildingComponentCatalog
