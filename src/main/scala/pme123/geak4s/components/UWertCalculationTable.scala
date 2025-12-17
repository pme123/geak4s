package pme123.geak4s.components

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import pme123.geak4s.domain.uwert.*
import pme123.geak4s.state.{UWertState, AppState}

/**
 * Reusable U-Wert calculation table component
 * Displays material layers and calculates thermal transmittance (U-value)
 * Now uses UWertState for persistence
 */
object UWertCalculationTable:

  def apply(calculationId: String): HtmlElement =
    val calcSignal = UWertState.getCalculation(calculationId)

    div(
      className := "calculation-table-group",
      marginBottom := "2rem",

      // Component selector
      renderComponentSelector(calculationId, calcSignal),

      // Tables - only shown when a component is selected
      child <-- calcSignal.map {
        case Some(calc) if calc.componentLabel.nonEmpty =>
          buildingComponents.find(_.label == calc.componentLabel) match
            case Some(component) =>
              div(
                display := "flex",
                gap := "2rem",
                marginTop := "1.5rem",

                // IST table (left)
                div(
                  flex := "1",
                  renderTable("IST", calculationId, component, calc.istCalculation)
                ),

                // SOLL table (right)
                div(
                  flex := "1",
                  renderTable("SOLL", calculationId, component, calc.sollCalculation)
                )
              )
            case None =>
              div(
                marginTop := "1rem",
                marginBottom := "1rem",
                Label("Fehler: Bauteil nicht gefunden")
              )
        case _ =>
          div(
            marginTop := "1rem",
            marginBottom := "1rem",
            Label("Bitte wählen Sie ein Bauteil aus der Liste oben.")
          )
      },

      // Delete button
      div(
        marginTop := "1rem",
        textAlign := "right",
        Button(
          _.design := ButtonDesign.Negative,
          _.icon := IconName.delete,
          _.events.onClick.mapTo(()) --> { _ =>
            UWertState.removeCalculation(calculationId)
            AppState.saveUWertCalculations()
          },
          "Berechnung löschen"
        )
      )
    )

  private def renderComponentSelector(
    calculationId: String,
    calcSignal: Signal[Option[UWertCalculation]]
  ): HtmlElement =
    div(
      className := "component-selector",
      marginBottom := "1.5rem",
      display := "flex",
      gap := "2rem",
      alignItems := "flex-end",

      // Bauteil selector
      div(
        flex := "1",

        Label(
          display := "block",
          marginBottom := "0.5rem",
          fontWeight := "600",
          "Beschrieb Bauteil"
        ),

        Select(
          _.value <-- calcSignal.map(_.map(_.componentLabel).getOrElse("")),
          _.events.onChange.mapToValue --> Observer[String] { label =>
            if label.nonEmpty then
              buildingComponents.find(_.label == label).foreach { component =>
                UWertState.updateComponent(calculationId, component, None)
                AppState.saveUWertCalculations()
              }
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
      ),

      // BWert selector - only shown when component is selected
      child <-- calcSignal.map {
        case Some(calc) if calc.componentLabel.nonEmpty =>
          buildingComponents.find(_.label == calc.componentLabel) match
            case Some(component) =>
              div(
                flex := "1",

                Label(
                  display := "block",
                  marginBottom := "0.5rem",
                  fontWeight := "600",
                  "b-Wert"
                ),

                Select(
                  _.value <-- calcSignal.map(_.flatMap(_.bWertName).getOrElse("")),
                  _.events.onChange.mapToValue --> Observer[String] { bWertName =>
                    if bWertName.nonEmpty then
                      BWert.values.find(_.name == bWertName).foreach { bWert =>
                        UWertState.updateBFactor(calculationId, bWert.bValue)
                        UWertState.updateCalculation(calculationId, _.copy(bWertName = Some(bWertName)))
                        AppState.saveUWertCalculations()
                      }
                  },

                  Select.option(
                    _.value := "",
                    "-- b-Wert auswählen --"
                  ),

                  BWert.getByComponentType(component.compType).map { bWert =>
                    Select.option(
                      _.value := bWert.name,
                      s"${bWert.name} (${bWert.bValue})"
                    )
                  }
                )
              )
            case None => emptyNode
        case _ =>
          emptyNode
      }
    )

  private def renderTable(
    tableType: String, // "IST" or "SOLL"
    calculationId: String,
    component: BuildingComponent,
    tableData: UWertTableData
  ): HtmlElement =
    div(
      // Table title
      div(
        marginBottom := "1rem",
        Title(
          _.level := TitleLevel.H4,
          s"U-Wert Berechnung $tableType"
        )
      ),

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
          tableData.materials.map { layer =>
            renderMaterialRow(calculationId, tableType, layer, component.compType)
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
              f"${tableData.rTotal}%.2f"
            )
          ),

          // b-Factor row
          tr(
            td(border := "1px solid #e0e0e0", padding := "0.5rem", colSpan := 4, textAlign := "right", "b-Faktor"),
            td(
              border := "1px solid #e0e0e0",
              padding := "0.5rem",
              tableData.bFactor.toString
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
              f"${tableData.uValueWithoutB}%.2f"
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
              f"${tableData.uValue}%.2f"
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
    calculationId: String,
    tableType: String,
    layer: MaterialLayer,
    componentType: ComponentType
  ): HtmlElement =
    val bgColor = if !layer.isEditable then "#f5f5f5" else "white"

    def updateMaterials(updater: MaterialLayer => MaterialLayer): Unit =
      if tableType == "IST" then
        UWertState.updateCalculation(calculationId, calc =>
          calc.copy(istCalculation = calc.istCalculation.copy(
            materials = calc.istCalculation.materials.map { m =>
              if m.nr == layer.nr then updater(m) else m
            }
          ))
        )
      else
        UWertState.updateCalculation(calculationId, calc =>
          calc.copy(sollCalculation = calc.sollCalculation.copy(
            materials = calc.sollCalculation.materials.map { m =>
              if m.nr == layer.nr then updater(m) else m
            }
          ))
        )
      AppState.saveUWertCalculations()

    tr(
      // Nr
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "center",
        backgroundColor := bgColor,
        layer.nr.toString
      ),

      // Description - with material selector for editable rows
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        backgroundColor := bgColor,
        if layer.isEditable then
          Select(
            _.value := layer.description,
            _.events.onChange.mapToValue --> Observer[String] { materialName =>
              if materialName.nonEmpty then
                BuildingComponentCatalog.components.find(_.name == materialName).foreach { material =>
                  updateMaterials(_.copy(
                    description = material.name,
                    lambda = material.thermalConductivity
                  ))
                }
              else
                // Clear the row if empty option selected
                updateMaterials(_.copy(description = "", lambda = 0.0, thickness = 0.0))
            },
            width := "100%",

            Select.option(
              _.value := "",
              "-- Material auswählen --"
            ),

            BuildingComponentCatalog.getByComponentType(componentType).map { material =>
              Select.option(
                _.value := material.name,
                s"${material.name} (λ = ${material.thermalConductivity})"
              )
            }
          )
        else
          span(layer.description)
      ),

      // Thickness (d in m)
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "right",
        backgroundColor := bgColor,
        if layer.isEditable then
          Input(
            _.value := (if layer.thickness == 0.0 then "" else layer.thickness.toString),
            onBlur.mapToValue --> Observer[String] { value =>
              updateMaterials(_.copy(thickness = value.toDoubleOption.getOrElse(0.0)))
            },
            width := "100%"
          )
        else
          span(layer.thickness.toString)
      ),

      // Lambda (λ)
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "right",
        backgroundColor := bgColor,
        if layer.isEditable then
          Input(
            _.disabled := true,
            _.value := (if layer.lambda == 0.0 then "" else layer.lambda.toString),
            width := "100%"
          )
        else
          span(layer.lambda.toString)
      ),

      // R value (d/λ)
      td(
        border := "1px solid #e0e0e0",
        padding := "0.5rem",
        textAlign := "right",
        backgroundColor := bgColor,
        fontWeight := (if !layer.isEditable then "600" else "normal"),
        f"${layer.rValue}%.2f"
      )
    )

end UWertCalculationTable

