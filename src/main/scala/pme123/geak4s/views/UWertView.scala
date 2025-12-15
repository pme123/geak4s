package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import pme123.geak4s.domain.uwert.*
import pme123.geak4s.state.AppState

/**
 * U-Wert (U-Value) calculation view
 * Allows users to select building components and calculate thermal transmittance
 */
object UWertView:

  // State for the current calculation
  private val selectedComponent = Var[Option[BuildingComponent]](None)
  private val materials = Var[List[MaterialRow]](List.empty)
  private val bFactor = Var[Double](0.0)
  
  case class MaterialRow(
    nr: Int,
    description: String,
    thickness: Double,  // d in m
    lambda: Double,     // λ
    isEditable: Boolean = true
  ):
    def rValue: Double = if lambda != 0 then thickness / lambda else 0.0
  
  def apply(): HtmlElement =
    div(
      className := "uwert-view",
      
      Card(
        _.slots.header := CardHeader(
          _.titleText := "U-Wert-Berechnung IST",
          _.subtitleText := "Wärmedurchgangskoeffizient berechnen"
        ),
        
        div(
          className := "card-content",
          padding := "1.5rem",
          
          // Component selector
          renderComponentSelector(),
          
          // Calculation table
          child <-- selectedComponent.signal.map {
            case Some(component) => renderCalculationTable(component)
            case None => div(
              marginTop := "1rem",
              Label("Bitte wählen Sie ein Bauteil aus der Liste oben.")
            )
          }
        )
      )
    )
  
  private def renderComponentSelector(): HtmlElement =
    div(
      className := "component-selector",
      marginBottom := "1.5rem",
      
      Label(
        display := "block",
        marginBottom := "0.5rem",
        fontWeight := "600",
        "Beschrieb Bauteil"
      ),
      
      Select(
        _.events.onChange.mapToValue --> Observer[String] { label =>
          if label.nonEmpty then
            val component = buildingComponents.find(_.label == label)
            selectedComponent.set(component)
            component.foreach(initializeMaterials)
          else
            selectedComponent.set(None)
            materials.set(List.empty)
        },

        Select.option(
          _.value := "",
          "-- Bauteil auswählen --"
        ),

        buildingComponents.map { component =>
          Select.option(
            _.value := component.label,
            component.label
          )
        }
      )
    )
  
  private def initializeMaterials(component: BuildingComponent): Unit =
    val rows = List(
      // Row 1: Heat transfer from inside
      MaterialRow(
        nr = 1,
        description = component.heatTransferFromInside.label,
        thickness = component.heatTransferFromInside.thicknessInM,
        lambda = component.heatTransferFromInside.lambda,
        isEditable = false
      )
    ) ++ 
    // Rows 2-8: Editable material layers (initially empty)
    (2 to 8).map { nr =>
      MaterialRow(
        nr = nr,
        description = "",
        thickness = 0.0,
        lambda = 0.0,
        isEditable = true
      )
    }.toList ++
    List(
      // Row 9: Heat transfer to outside
      MaterialRow(
        nr = 9,
        description = component.heatTransferToOutside.label,
        thickness = component.heatTransferToOutside.thicknessInM,
        lambda = component.heatTransferToOutside.lambda,
        isEditable = false
      )
    )
    
    materials.set(rows)
  
  private def renderCalculationTable(component: BuildingComponent): HtmlElement =
    div(
      className := "calculation-table",
      
      // Component title
      div(
        marginBottom := "1rem",
        Title(
          _.level := TitleLevel.H4,
          s"Bauteil: ${component.label}"
        )
      ),
      
      // Table
      table(
        width := "100%",
        border := "1px solid #e0e0e0",
        borderCollapse := "collapse",
        
        // Header
        thead(
          backgroundColor := "#f5f5f5",
          tr(
            th(border := "1px solid #e0e0e0", padding := "0.5rem", "Nr."),
            th(border := "1px solid #e0e0e0", padding := "0.5rem", "Was"),
            th(border := "1px solid #e0e0e0", padding := "0.5rem", "d in m"),
            th(border := "1px solid #e0e0e0", padding := "0.5rem", "λ"),
            th(border := "1px solid #e0e0e0", padding := "0.5rem", "d/λ (R)")
          )
        ),

        // Body - Material rows
        tbody(
          children <-- materials.signal.map { rows =>
            rows.map(row => renderMaterialRow(row))
          }
        ),

        // Footer - Totals and results
        tfoot(
          backgroundColor := "#f5f5f5",

          // R total row
          tr(
            td(border := "1px solid #e0e0e0", padding := "0.5rem", colSpan := 4, textAlign := "right", fontWeight := "600", "R total ="),
            td(
              border := "1px solid #e0e0e0",
              padding := "0.5rem",
              textAlign := "right",
              fontWeight := "600",
              child.text <-- materials.signal.map { rows =>
                f"${rows.map(_.rValue).sum}%.2f"
              }
            )
          ),

          // b-Factor row
          tr(
            td(border := "1px solid #e0e0e0", padding := "0.5rem", colSpan := 4, textAlign := "right", "b-Faktor"),
            td(
              border := "1px solid #e0e0e0",
              padding := "0.5rem",
              Input(
                _.value <-- bFactor.signal.map(_.toString),
                _.events.onInput.mapToValue --> Observer[String] { value =>
                  bFactor.set(value.toDoubleOption.getOrElse(0.0))
                },
                width := "100%",
                textAlign := "right"
              )
            )
          ),

          // U-Wert without b-factor
          tr(
            td(border := "1px solid #e0e0e0", padding := "0.5rem", colSpan := 4, textAlign := "right", "U-Wert (ohne b-Faktor) ="),
            td(
              border := "1px solid #e0e0e0",
              padding := "0.5rem",
              textAlign := "right",
              fontWeight := "600",
              child.text <-- materials.signal.map { rows =>
                val rTotal = rows.map(_.rValue).sum
                if rTotal != 0 then f"${1.0 / rTotal}%.2f" else "0.00"
              }
            )
          ),

          // U-Wert with b-factor
          tr(
            td(border := "1px solid #e0e0e0", padding := "0.5rem", colSpan := 4, textAlign := "right", fontWeight := "600", "U-Wert (mit b-Faktor) ="),
            td(
              border := "1px solid #e0e0e0",
              padding := "0.5rem",
              textAlign := "right",
              fontWeight := "600",
              backgroundColor := "#fff3cd",
              child.text <-- materials.signal.combineWith(bFactor.signal).map { case (rows, bf) =>
                val rTotal = rows.map(_.rValue).sum
                if rTotal != 0 then f"${bf / rTotal}%.2f" else "0.00"
              }
            )
          ),

          // Unit row
          tr(
            td(border := "1px solid #e0e0e0", padding := "0.5rem", colSpan := 5, textAlign := "right", fontStyle := "italic", "W/m²K")
          )
        )
      )
    )

  private def renderMaterialRow(row: MaterialRow): HtmlElement =
    val bgColor = if !row.isEditable then "#f5f5f5" else "white"
    tr(
      // Nr
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "center",
        backgroundColor <-- Val(bgColor),
        row.nr.toString
      ),

      // Description
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        backgroundColor <-- Val(bgColor),
        if row.isEditable then
          Input(
            _.value := row.description,
            _.events.onInput.mapToValue --> Observer[String] { value =>
              materials.update { rows =>
                rows.map { r =>
                  if r.nr == row.nr then r.copy(description = value) else r
                }
              }
            },
            width := "100%"
          )
        else
          span(row.description)
      ),

      // Thickness (d in m)
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "right",
        backgroundColor <-- Val(bgColor),
        if row.isEditable then
          Input(
            _.value := (if row.thickness == 0.0 then "" else row.thickness.toString),
            _.events.onInput.mapToValue --> Observer[String] { value =>
              materials.update { rows =>
                rows.map { r =>
                  if r.nr == row.nr then r.copy(thickness = value.toDoubleOption.getOrElse(0.0)) else r
                }
              }
            },
            width := "100%"
          )
        else
          span(row.thickness.toString)
      ),

      // Lambda (λ)
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "right",
        backgroundColor <-- Val(bgColor),
        if row.isEditable then
          Input(
            _.value := (if row.lambda == 0.0 then "" else row.lambda.toString),
            _.events.onInput.mapToValue --> Observer[String] { value =>
              materials.update { rows =>
                rows.map { r =>
                  if r.nr == row.nr then r.copy(lambda = value.toDoubleOption.getOrElse(0.0)) else r
                }
              }
            },
            width := "100%"
          )
        else
          span(row.lambda.toString)
      ),

      // R value (d/λ)
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "right",
        backgroundColor <-- Val(bgColor),
        fontWeight <-- Val(if !row.isEditable then "600" else "normal"),
        f"${row.rValue}%.2f"
      )
    )

end UWertView

