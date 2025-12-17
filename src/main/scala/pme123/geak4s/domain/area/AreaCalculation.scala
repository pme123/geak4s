package pme123.geak4s.domain.area

/** Area calculation category types */
enum AreaCategory:
  case EBF                      // Energiebezugsfläche
  case DachGegenAussenluft      // Dach gegen Aussenluft
  case DeckeGegenUnbeheizt      // Decke gegen unbeheizt
  case WandGegenAussenluft      // Wand gegen Aussenluft
  case WandGegenErdreich        // Wand gegen Erdreich
  case WandGegenUnbeheizt       // Wand gegen unbeheizt
  case FensterUndTueren         // Fenster und Türen
  case BodenGegenErdreich       // Boden gegen Erdreich
  case BodenGegenUnbeheizt      // Boden gegen unbeheizt
  case BodenGegenAussen         // Boden gegen aussen

  def label: String = this match
    case EBF                   => "EBF"
    case DachGegenAussenluft   => "Dach gegen Aussenluft"
    case DeckeGegenUnbeheizt   => "Decke gegen unbeheizt"
    case WandGegenAussenluft   => "Wand gegen Aussenluft"
    case WandGegenErdreich     => "Wand gegen Erdreich"
    case WandGegenUnbeheizt    => "Wand gegen unbeheizt"
    case FensterUndTueren      => "Fenster und Türen"
    case BodenGegenErdreich    => "Boden gegen Erdreich"
    case BodenGegenUnbeheizt   => "Boden gegen unbeheizt"
    case BodenGegenAussen      => "Boden gegen aussen"

end AreaCategory

/** Single area entry (one row in the table) */
case class AreaEntry(
    nr: String,                      // Bauteil Nr. (e.g., "1", "D1", "W1", "F1")
    orientation: String,             // Ausrichtung (N, S, O, W, horizontal, etc.)
    description: String,             // Beschrieb
    length: Double,                  // Länge / Umfang [m]
    width: Double,                   // Breite / Höhe [m]
    area: Double,                    // Fläche [m2]
    quantity: Int,                   // Anzahl [Stk.]
    totalArea: Double,               // Fläche Total [m2]
    // SOLL values (new state)
    areaNew: Double,                 // Fläche Neu [m2]
    quantityNew: Int,                // Anzahl Neu [Stk.]
    totalAreaNew: Double,            // Fläche Total Neu [m2]
    descriptionNew: String           // Beschrieb Neu
):
  /** Calculate total area from individual values */
  def calculateTotalArea: Double = area * quantity
  
  /** Calculate new total area from new values */
  def calculateTotalAreaNew: Double = areaNew * quantityNew

end AreaEntry

object AreaEntry:
  /** Create a new empty entry */
  def empty(nr: String): AreaEntry = AreaEntry(
    nr = nr,
    orientation = "",
    description = "",
    length = 0.0,
    width = 0.0,
    area = 0.0,
    quantity = 1,
    totalArea = 0.0,
    areaNew = 0.0,
    quantityNew = 0,
    totalAreaNew = 0.0,
    descriptionNew = ""
  )

  /** Create entry with auto-calculated totals */
  def apply(
      nr: String,
      orientation: String,
      description: String,
      length: Double,
      width: Double,
      area: Double,
      quantity: Int,
      areaNew: Double,
      quantityNew: Int,
      descriptionNew: String
  ): AreaEntry =
    val totalArea = area * quantity
    val totalAreaNew = areaNew * quantityNew
    new AreaEntry(
      nr,
      orientation,
      description,
      length,
      width,
      area,
      quantity,
      totalArea,
      areaNew,
      quantityNew,
      totalAreaNew,
      descriptionNew
    )

end AreaEntry

/** Area calculation for a specific category */
case class AreaCalculation(
    category: AreaCategory,
    entries: List[AreaEntry]
):
  /** Total area IST (sum of all totalArea) */
  def totalAreaIst: Double = entries.map(_.totalArea).sum
  
  /** Total area SOLL (sum of all totalAreaNew) */
  def totalAreaSoll: Double = entries.map(_.totalAreaNew).sum
  
  /** Total quantity IST */
  def totalQuantityIst: Int = entries.map(_.quantity).sum
  
  /** Total quantity SOLL */
  def totalQuantitySoll: Int = entries.map(_.quantityNew).sum

end AreaCalculation

object AreaCalculation:
  /** Create empty calculation for a category */
  def empty(category: AreaCategory): AreaCalculation =
    AreaCalculation(category, List.empty)

end AreaCalculation

/** Complete building envelope area summary */
case class BuildingEnvelopeArea(
    ebf: AreaCalculation,
    dachGegenAussenluft: AreaCalculation,
    deckeGegenUnbeheizt: AreaCalculation,
    wandGegenAussenluft: AreaCalculation,
    wandGegenErdreich: AreaCalculation,
    wandGegenUnbeheizt: AreaCalculation,
    fensterUndTueren: AreaCalculation,
    bodenGegenErdreich: AreaCalculation,
    bodenGegenUnbeheizt: AreaCalculation,
    bodenGegenAussen: AreaCalculation
)

object BuildingEnvelopeArea:
  /** Create empty building envelope area */
  def empty: BuildingEnvelopeArea = BuildingEnvelopeArea(
    ebf = AreaCalculation.empty(AreaCategory.EBF),
    dachGegenAussenluft = AreaCalculation.empty(AreaCategory.DachGegenAussenluft),
    deckeGegenUnbeheizt = AreaCalculation.empty(AreaCategory.DeckeGegenUnbeheizt),
    wandGegenAussenluft = AreaCalculation.empty(AreaCategory.WandGegenAussenluft),
    wandGegenErdreich = AreaCalculation.empty(AreaCategory.WandGegenErdreich),
    wandGegenUnbeheizt = AreaCalculation.empty(AreaCategory.WandGegenUnbeheizt),
    fensterUndTueren = AreaCalculation.empty(AreaCategory.FensterUndTueren),
    bodenGegenErdreich = AreaCalculation.empty(AreaCategory.BodenGegenErdreich),
    bodenGegenUnbeheizt = AreaCalculation.empty(AreaCategory.BodenGegenUnbeheizt),
    bodenGegenAussen = AreaCalculation.empty(AreaCategory.BodenGegenAussen)
  )

end BuildingEnvelopeArea

