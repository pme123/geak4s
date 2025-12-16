package pme123.geak4s.components

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import pme123.geak4s.domain.uwert.*

/**
 * Reusable U-Wert calculation table component
 * Displays material layers and calculates thermal transmittance (U-value)
 */
object UWertCalculationTable:

  case class MaterialRow(
    nr: Int,
    description: String,
    thickness: Double,  // d in m
    lambda: Double,     // λ
    isEditable: Boolean = true
  ):
    def rValue: Double = if lambda != 0 then thickness / lambda else 0.0

  def apply(
    component: BuildingComponent,
    materials: Signal[List[MaterialRow]],
    bFactor: Signal[Double],
    onMaterialsUpdate: (List[MaterialRow] => List[MaterialRow]) => Unit,
    onBFactorChange: Double => Unit
  ): HtmlElement =
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
          children <-- materials.map { rows =>
            rows.map(row => renderMaterialRow(row, component.compType, onMaterialsUpdate))
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
              child.text <-- materials.map { rows =>
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
                _.value <-- bFactor.map(_.toString),
                _.events.onInput.mapToValue --> Observer[String] { value =>
                  onBFactorChange(value.toDoubleOption.getOrElse(0.0))
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
              child.text <-- materials.map { rows =>
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
              child.text <-- materials.combineWith(bFactor).map { case (rows, bf) =>
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

  private def renderMaterialRow(
    row: MaterialRow,
    componentType: ComponentType,
    onMaterialsUpdate: (List[MaterialRow] => List[MaterialRow]) => Unit
  ): HtmlElement =
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

      // Description - with material selector for editable rows
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        backgroundColor <-- Val(bgColor),
        if row.isEditable then
          Select(
            _.events.onChange.mapToValue --> Observer[String] { materialName =>
              if materialName.nonEmpty then
                BuildingComponentCatalog.components.find(_.name == materialName).foreach { material =>
                  onMaterialsUpdate { rows =>
                    rows.map { r =>
                      if r.nr == row.nr then
                        r.copy(
                          description = material.name,
                          lambda = material.thermalConductivity
                        )
                      else r
                    }
                  }
                }
              else
                // Clear the row if empty option selected
                onMaterialsUpdate { rows =>
                  rows.map { r =>
                    if r.nr == row.nr then
                      r.copy(description = "", lambda = 0.0)
                    else r
                  }
                }
            },
            width := "100%",

            Select.option(
              _.value := "",
              _.selected := row.description.isEmpty,
              "-- Material auswählen --"
            ),

            BuildingComponentCatalog.getByComponentType(componentType).map { material =>
              Select.option(
                _.value := material.name,
                _.selected := row.description == material.name,
                s"${material.name} (λ = ${material.thermalConductivity})"
              )
            }
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
            onBlur.mapToValue --> Observer[String] { value =>
              onMaterialsUpdate { rows =>
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
            _.disabled := true,
            _.value := (if row.lambda == 0.0 then "" else row.lambda.toString),
          /*  onBlur.mapToValue --> Observer[String] { value =>
              onMaterialsUpdate { rows =>
                rows.map { r =>
                  if r.nr == row.nr then r.copy(lambda = value.toDoubleOption.getOrElse(0.0)) else r
                }
              }
            },*/
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

end UWertCalculationTable

