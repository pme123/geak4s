package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import pme123.geak4s.components.AreaCalculationTable
import pme123.geak4s.domain.area.*
import pme123.geak4s.state.UWertState
import pme123.geak4s.domain.uwert.{ComponentType, UWertCalculation}

/**
 * Area calculation view (Flächenberechnung)
 * Allows users to calculate building envelope areas for IST and SOLL states
 * Dynamically generates tables based on U-Wert calculations
 */
object AreaView:

  // Map to store area entries for each U-Wert calculation ID
  private val areaEntriesByUWertId = scala.collection.mutable.Map[String, Var[List[AreaEntry]]]()
  private val ebfEntries = Var[List[AreaEntry]](List.empty)

  // Get or create area entries for a U-Wert calculation
  private def getAreaEntries(uwertId: String): Var[List[AreaEntry]] =
    areaEntriesByUWertId.getOrElseUpdate(uwertId, Var(List.empty))

  def apply(): HtmlElement =
    div(
      className := "area-view",
      Card(
        className := "project-view",
        maxWidth  := "100%",
        display   := "flex",
        div(
          className := "card-content",
          padding   := "1.5rem",
          // EBF (always required) - styled like other building components
          renderEBFGroup(),
          // Render one AreaCalculationTable per U-Wert calculation
          children <-- UWertState.calculations.signal.map { calculations =>
            calculations.zipWithIndex.map { case (calc, index) =>
              renderCalculationGroup(calc, index)
            }
          }
        )
      )
    )

  /** Render EBF group with same styling as other building components */
  private def renderEBFGroup(): HtmlElement =
    div(
      marginBottom := "3rem",
      padding := "1.5rem",
      backgroundColor := ComponentType.EBF.color,
      borderRadius := "8px",
      border := "1px solid #ddd",

      // Header with component label
      div(
        marginBottom := "1.5rem",
        Title(
          _.level := TitleLevel.H3,
          ComponentType.EBF.label
        )
      ),

      // Area calculation table
      div(
        backgroundColor := "white",
        padding := "1rem",
        borderRadius := "4px",
        marginBottom := "1.5rem",
        AreaCalculationTable(ComponentType.EBF, ebfEntries)
      )
    )

  /** Render a calculation group (AreaCalculationTable + U-Wert summary) */
  private def renderCalculationGroup(calc: UWertCalculation, index: Int): HtmlElement =
    if calc.componentLabel.nonEmpty then
      div(
        marginBottom := "3rem",
        padding := "1.5rem",
        backgroundColor := calc.componentType.color,
        borderRadius := "8px",
        border := "1px solid #ddd",

        // Header with component label
        div(
          marginBottom := "1.5rem",
          Title(
            _.level := TitleLevel.H3,
            calc.componentLabel
          )
        ),

        // Area calculation table
        div(
          backgroundColor := "white",
          padding := "1rem",
          borderRadius := "4px",
          marginBottom := "1.5rem",
          AreaCalculationTable(calc.componentType, getAreaEntries(calc.id))
        )
      )
    else
      div(display := "none")


end AreaView

