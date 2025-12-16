package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import pme123.geak4s.domain.uwert.*
import pme123.geak4s.state.AppState
import pme123.geak4s.components.UWertCalculationTable
import pme123.geak4s.components.UWertCalculationTable.MaterialRow

/**
 * U-Wert (U-Value) calculation view
 * Allows users to select building components and calculate thermal transmittance
 */
object UWertView:

  // State for the current calculation
  private val selectedComponent = Var[Option[BuildingComponent]](None)
  private val materials = Var[List[MaterialRow]](List.empty)
  private val bFactor = Var[Double](0.0)
  private val selectedRowForMaterial = Var[Option[Int]](None)  // Track which row is being edited
  
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

          // Material selector (shown when a row is selected)
          child <-- selectedComponent.signal.combineWith(selectedRowForMaterial.signal).map {
            case (Some(component), Some(rowNr)) => renderMaterialSelector(component, rowNr)
            case _ => emptyNode
          },

          // Calculation table
          child <-- selectedComponent.signal.map {
            case Some(component) =>
              UWertCalculationTable(
                component = component,
                materials = materials.signal,
                bFactor = bFactor.signal,
                onMaterialsUpdate = materials.update,
                onBFactorChange = bFactor.set,
                onSelectMaterial = rowNr => selectedRowForMaterial.set(Some(rowNr))
              )
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

  private def renderMaterialSelector(component: BuildingComponent, rowNr: Int): HtmlElement =
    div(
      className := "material-selector",
      marginBottom := "1.5rem",
      padding := "1rem",
      backgroundColor := "#f0f8ff",
      border := "1px solid #0078d4",
      borderRadius := "4px",

      div(
        display := "flex",
        justifyContent := "space-between",
        alignItems := "center",
        marginBottom := "0.5rem",

        Label(
          fontWeight := "600",
          s"Material auswählen für Zeile $rowNr"
        ),

        Button(
          _.design := ButtonDesign.Transparent,
          _.icon := IconName.decline,
          _.events.onClick --> { _ =>
            selectedRowForMaterial.set(None)
          }
        )
      ),

      Label(
        display := "block",
        marginBottom := "0.5rem",
        "Baumaterial"
      ),

      Select(
        _.events.onChange.mapToValue --> Observer[String] { materialName =>
          if materialName.nonEmpty then
            BuildingComponentCatalog.components.find(_.name == materialName).foreach { material =>
              materials.update { rows =>
                rows.map { r =>
                  if r.nr == rowNr then
                    r.copy(
                      description = material.name,
                      lambda = material.thermalConductivity
                    )
                  else r
                }
              }
              selectedRowForMaterial.set(None)
            }
        },

        Select.option(
          _.value := "",
          "-- Material auswählen --"
        ),

        BuildingComponentCatalog.getByComponentType(component.compType).map { material =>
          Select.option(
            _.value := material.name,
            s"${material.name} (λ = ${material.thermalConductivity})"
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
        lambda = component.heatTransferFromInside.thermalConductivity,
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
        lambda = component.heatTransferToOutside.thermalConductivity,
        isEditable = false
      )
    )
    
    materials.set(rows)

end UWertView

