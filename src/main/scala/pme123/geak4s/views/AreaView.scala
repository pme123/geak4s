package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import pme123.geak4s.components.AreaCalculationTable
import pme123.geak4s.domain.area.*

/**
 * Area calculation view (Flächenberechnung)
 * Allows users to calculate building envelope areas for IST and SOLL states
 * Automatically generates tables based on building components
 */
object AreaView:

  // State for all area categories
  private val ebfEntries = Var[List[AreaEntry]](initializeEBF())
  private val dachGegenAussenluftEntries = Var[List[AreaEntry]](List.empty)
  private val deckeGegenUnbeheiztEntries = Var[List[AreaEntry]](List.empty)
  private val wandGegenAussenluftEntries = Var[List[AreaEntry]](List.empty)
  private val wandGegenErdreichEntries = Var[List[AreaEntry]](List.empty)
  private val wandGegenUnbeheiztEntries = Var[List[AreaEntry]](List.empty)
  private val fensterUndTuerenEntries = Var[List[AreaEntry]](List.empty)
  private val bodenGegenErdreichEntries = Var[List[AreaEntry]](List.empty)
  private val bodenGegenUnbeheiztEntries = Var[List[AreaEntry]](List.empty)
  private val bodenGegenAussenEntries = Var[List[AreaEntry]](List.empty)

  def apply(): HtmlElement =
    div(
      className := "area-view",
      Card(
        className := "project-view",
        maxWidth  := "100%",
        display   := "flex",
        _.slots.header := CardHeader(
          _.titleText    := "Flächenberechnung",
          _.subtitleText := "Zusammenstellung der Gebäudehülle (IST und SOLL)"
        ),
        div(
          className := "card-content",
          padding   := "1.5rem",

          // EBF (always required)
          AreaCalculationTable(AreaCategory.EBF, ebfEntries),

          // Dach gegen Aussenluft
          AreaCalculationTable(AreaCategory.DachGegenAussenluft, dachGegenAussenluftEntries),

          // Decke gegen unbeheizt
          AreaCalculationTable(AreaCategory.DeckeGegenUnbeheizt, deckeGegenUnbeheiztEntries),

          // Wand gegen Aussenluft
          AreaCalculationTable(AreaCategory.WandGegenAussenluft, wandGegenAussenluftEntries),

          // Wand gegen Erdreich
          AreaCalculationTable(AreaCategory.WandGegenErdreich, wandGegenErdreichEntries),

          // Wand gegen unbeheizt
          AreaCalculationTable(AreaCategory.WandGegenUnbeheizt, wandGegenUnbeheiztEntries),

          // Fenster und Türen
          AreaCalculationTable(AreaCategory.FensterUndTueren, fensterUndTuerenEntries),

          // Boden gegen Erdreich
          AreaCalculationTable(AreaCategory.BodenGegenErdreich, bodenGegenErdreichEntries),

          // Boden gegen unbeheizt
          AreaCalculationTable(AreaCategory.BodenGegenUnbeheizt, bodenGegenUnbeheiztEntries),

          // Boden gegen aussen
          AreaCalculationTable(AreaCategory.BodenGegenAussen, bodenGegenAussenEntries),

          // Summary section
          renderSummary()
        )
      )
    )

  /** Initialize EBF with default entries */
  private def initializeEBF(): List[AreaEntry] =
    List(
      AreaEntry("1", "", "UG", 0.0, 0.0, 101.0, 1, 0.0, 0, ""),
      AreaEntry("2", "", "EG", 0.0, 0.0, 197.0, 1, 0.0, 0, ""),
      AreaEntry("3", "", "1. OG", 0.0, 0.0, 200.0, 1, 0.0, 0, ""),
      AreaEntry("4", "", "2. OG", 0.0, 0.0, 200.0, 1, 0.0, 0, ""),
      AreaEntry("5", "", "3. OG", 0.0, 0.0, 200.0, 1, 0.0, 0, ""),
      AreaEntry("6", "", "DG", 0.0, 0.0, 200.0, 1, 0.0, 0, ""),
      AreaEntry("7", "", "Galerie", 0.0, 0.0, 57.0, 1, 0.0, 0, "")
    )

  /** Render overall summary */
  private def renderSummary(): HtmlElement =
    div(
      marginTop := "2rem",
      padding := "1.5rem",
      backgroundColor := "#e3f2fd",
      borderRadius := "8px",
      
      Title(
        _.level := TitleLevel.H3,
        "Gesamtübersicht"
      ),
      
      div(
        marginTop := "1rem",
        display := "flex",
        gap := "1rem",
        
        // IST Summary
        div(
          flex := "1",
          padding := "1rem",
          backgroundColor := "white",
          borderRadius := "4px",
          
          div(
            fontWeight := "600",
            fontSize := "1.1rem",
            marginBottom := "0.5rem",
            "IST-Zustand"
          ),
          
          child <-- Signal.combineSeq(List(
            ebfEntries.signal,
            dachGegenAussenluftEntries.signal,
            deckeGegenUnbeheiztEntries.signal,
            wandGegenAussenluftEntries.signal,
            wandGegenErdreichEntries.signal,
            wandGegenUnbeheiztEntries.signal,
            fensterUndTuerenEntries.signal,
            bodenGegenErdreichEntries.signal,
            bodenGegenUnbeheiztEntries.signal,
            bodenGegenAussenEntries.signal
          )).map { allEntries =>
            val totalArea = allEntries.flatten.map(_.totalArea).sum
            div(f"Gesamtfläche: $totalArea%.2f m²")
          }
        ),
        
        // SOLL Summary
        div(
          flex := "1",
          padding := "1rem",
          backgroundColor := "white",
          borderRadius := "4px",
          
          div(
            fontWeight := "600",
            fontSize := "1.1rem",
            marginBottom := "0.5rem",
            "SOLL-Zustand"
          ),
          
          child <-- Signal.combineSeq(List(
            ebfEntries.signal
          )).map { _ =>
            div("Noch nicht definiert")
          }
        )
      )
    )

end AreaView

