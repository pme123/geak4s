package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import pme123.geak4s.components.UWertCalculationTable

/**
 * U-Wert (U-Value) calculation view
 * Allows users to select building components and calculate thermal transmittance
 * Supports multiple calculation tables
 */
object UWertView:

  // State for managing multiple calculation tables
  private val tables = Var[List[Int]](List(1))
  private var nextId = 2

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

          // Render all calculation tables with stable keys
          children <-- tables.signal.split(identity) { (id, _, _) =>
            UWertCalculationTable()
          },

          // Add table button
          div(
            marginTop := "1.5rem",
            textAlign := "center",
            Button(
              _.design := ButtonDesign.Emphasized,
              _.icon := IconName.add,
              _.events.onClick.mapTo(()) --> { _ =>
                val newId = nextId
                nextId += 1
                tables.update(_ :+ newId)
              },
              "Weitere Berechnung hinzufügen"
            )
          )
        )
      )
    )

end UWertView

