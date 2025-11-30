package pme123.geak4s.components

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*
import pme123.geak4s.domain.Address
import pme123.geak4s.services.{GeoAdminService, AddressResult}
import scala.concurrent.ExecutionContext.Implicits.global
import pme123.geak4s.validation.Validators
import org.scalajs.dom
import scala.scalajs.js

/** Reusable Address field component */
object AddressField:

  /** Convert WGS84 coordinates (lat/lon) to Swiss LV95 coordinates (east/north)
    * Approximate conversion using the official formulas from swisstopo
    */
  private def wgs84ToLV95(lat: Double, lon: Double): (Double, Double) =
    // Convert to auxiliary values
    val latAux = (lat * 3600 - 169028.66) / 10000.0
    val lonAux = (lon * 3600 - 26782.5) / 10000.0

    // Calculate east coordinate
    val east = 2600072.37 +
      211455.93 * lonAux -
      10938.51 * lonAux * latAux -
      0.36 * lonAux * latAux * latAux -
      44.54 * lonAux * lonAux * lonAux

    // Calculate north coordinate
    val north = 1200147.07 +
      308807.95 * latAux +
      3745.25 * lonAux * lonAux +
      76.63 * latAux * latAux -
      194.56 * lonAux * lonAux * latAux +
      119.79 * latAux * latAux * latAux

    (east, north)

  def apply(
    label: String = "Address",
    required: Boolean = false,
    addressSignal: Signal[Address],
    onAddressChange: Address => Unit,
    showStreet: Boolean = true,
    showHouseNumber: Boolean = true,
    showZipCity: Boolean = true,
    showCountry: Boolean = true
  ): HtmlElement =
    // Create a Var to track the current address
    val addressVar = Var(Address.empty)

    // Address search state
    val searchSuggestions = Var(List.empty[AddressResult])
    val showSearchSuggestions = Var(false)
    var searchTimer: Option[Int] = None

    // State for map dialog
    val showMapDialog = Var(false)
    val mapUrl = Var("")

    // Helper function to format address as string
    def formatAddress(addr: Address): String =
      val parts = List(
        addr.street,
        addr.houseNumber,
        addr.zipCode,
        addr.city
      ).flatten
      if parts.nonEmpty then parts.mkString(" ") else ""

    // State for search field - starts empty, only filled by user input or search selection
    val searchFieldValue = Var("")

    div(
      className := "address-field",

      // Update the Var when the signal changes
      addressSignal --> addressVar.writer,

      // Section Label with Search Field and Map Button
      div(
        display := "flex",
        alignItems := "center",
        gap := "1rem",
        marginBottom := "0.5rem",

        Label(
          fontWeight := "600",
          fontSize := "1rem",
          whiteSpace := "nowrap",
          label,
          if required then
            span(
              color := "red",
              marginLeft := "0.25rem",
              "*"
            )
          else emptyNode
        ),

        // Search Field with Icon - takes remaining space
        div(
          flex := "1",
          display := "flex",
          alignItems := "center",
          position := "relative",

          Input(
            _.placeholder := "Suche die Adresse",
            _.showClearIcon := true,
            _.value <-- searchFieldValue.signal,
            width := "100%",
            _.events.onInput.mapToValue --> Observer[String] { searchValue =>
              dom.console.log(s"AddressField: onInput - searchValue='$searchValue'")
              searchFieldValue.set(searchValue)

              // Cancel any pending search timer
              searchTimer.foreach(dom.window.clearTimeout)

              if searchValue.trim.isEmpty then
                showSearchSuggestions.set(false)
              else
                // Wait 1 second before searching
                searchTimer = Some(dom.window.setTimeout(() => {
                  dom.console.log(s"AddressField: Searching for '$searchValue'")
                  GeoAdminService.searchAddress(searchValue).foreach { results =>
                    dom.console.log(s"AddressField: Got ${results.length} results")
                    searchSuggestions.set(results)
                    showSearchSuggestions.set(results.nonEmpty)
                  }
                }, 1000))
            },
            // Add icon slot
            Icon(_.name := IconName.search, slot := "icon")
          ),

          // Search Suggestions Dropdown
          child <-- showSearchSuggestions.signal.combineWith(searchSuggestions.signal).map {
            case (true, suggestions) if suggestions.nonEmpty =>
              dom.console.log(s"AddressField: Rendering ${suggestions.length} suggestions")
              div(
                position := "absolute",
                top := "100%",
                left := "0",
                right := "0",
                backgroundColor := "white",
                border := "1px solid #ccc",
                borderRadius := "4px",
                boxShadow := "0 2px 8px rgba(0,0,0,0.15)",
                maxHeight := "300px",
                overflowY := "auto",
                zIndex := "1000",
                marginTop := "2px",

                suggestions.map { result =>
                  div(
                    padding := "8px 12px",
                    cursor := "pointer",
                    borderBottom := "1px solid #f0f0f0",
                    onMouseEnter --> Observer[dom.MouseEvent] { e =>
                      e.target.asInstanceOf[dom.HTMLElement].style.backgroundColor = "#f5f5f5"
                    },
                    onMouseLeave --> Observer[dom.MouseEvent] { e =>
                      e.target.asInstanceOf[dom.HTMLElement].style.backgroundColor = "white"
                    },
                    onMouseDown --> Observer[dom.MouseEvent] { e =>
                      e.preventDefault() // Prevent input blur
                      val address = GeoAdminService.toAddress(result)
                      val formatted = formatAddress(address)
                      dom.console.log(s"AddressField: Selected address - formatted='$formatted', address=$address")
                      onAddressChange(address)
                      showSearchSuggestions.set(false)
                      // Fill search field with selected address
                      searchFieldValue.set(formatted)
                      dom.console.log(s"AddressField: Set searchFieldValue to '$formatted', current value=${searchFieldValue.now()}")
                    },
                    // Remove <b> and </b> tags from label
                    result.label.replace("<b>", "").replace("</b>", "")
                  )
                }
              )
            case _ => emptyNode
          }
        ),

        // Show on Map Button - visible if address has any data
        child <-- addressSignal.map { address =>
          val hasAddress = address.street.isDefined || address.zipCode.isDefined || address.city.isDefined
          if hasAddress then
            Button(
              _.design := ButtonDesign.Transparent,
              _.icon := IconName.map,
              _.tooltip := "Show on Map",
              _.events.onClick.mapTo(address) --> Observer[Address] { addr =>
                // Build search string from address
                val searchString = formatAddress(addr)

                // Always use search string - the map will search for the address
                val encodedSearch = js.Dynamic.global.encodeURIComponent(searchString).asInstanceOf[String]
                val url = s"https://map.geo.admin.ch/#/map?lang=de&z=10&topic=ech&layers=ch.swisstopo.amtliches-gebaeudeadressverzeichnis&bgLayer=ch.swisstopo.pixelkarte-farbe&swisssearch=$encodedSearch"

                dom.console.log(s"AddressField: Opening map with search='$searchString', url=$url")
                mapUrl.set(url)
                showMapDialog.set(true)
              }
            )
          else
            emptyNode
        }
      ),

      // Map Dialog
      Dialog(
        _.headerText := "Karte",
        _.open <-- showMapDialog.signal,
        styleAttr := "width: 90vw; max-width: 1400px;",

        // Dialog content - iframe with map
        child <-- mapUrl.signal.map { url =>
          if url.nonEmpty then
            htmlTag("iframe")(
              src := url,
              width := "100%",
              height := "700px",
              border := "none"
            )
          else
            emptyNode
        },

        // Footer with close button
        div(
          slot := "footer",
          display := "flex",
          justifyContent := "flex-end",

          Button(
            _.design := ButtonDesign.Emphasized,
            "Schliessen",
            _.events.onClick.mapTo(false) --> showMapDialog.writer
          )
        )
      ),

      // Street and House Number Row
      if showStreet || showHouseNumber then
        div(
          className := "form-row",

          if showStreet then
            div(
              className := "form-field",
              Label(
                display := "block",
                marginBottom := "0.25rem",
                fontWeight := "600",
                "Street",
                if required then
                  span(
                    color := "red",
                    marginLeft := "0.25rem",
                    "*"
                  )
                else emptyNode
              ),
              Input(
                _.placeholder := "e.g., Musterstrasse",
                _.value <-- addressSignal.map(_.street.getOrElse("")),
                _.events.onChange.mapToValue --> Observer[String] { value =>
                  val currentAddress = addressVar.now()
                  onAddressChange(currentAddress.copy(street = if value.isEmpty then None else Some(value)))
                }
              )
            )
          else emptyNode,

          if showHouseNumber then
            div(
              className := "form-field",
              Label(
                display := "block",
                marginBottom := "0.25rem",
                fontWeight := "600",
                "House Number",
                if required then
                  span(
                    color := "red",
                    marginLeft := "0.25rem",
                    "*"
                  )
                else emptyNode
              ),
              Input(
                _.placeholder := "e.g., 123",
                _.value <-- addressSignal.map(_.houseNumber.getOrElse("")),
                _.events.onChange.mapToValue --> Observer[String] { value =>
                  val currentAddress = addressVar.now()
                  onAddressChange(currentAddress.copy(houseNumber = if value.isEmpty then None else Some(value)))
                }
              )
            )
          else emptyNode
        )
      else emptyNode,

      // ZIP and City Row with Auto-Complete
      if showZipCity then
        ZipCityField(
          zipLabel = "ZIP Code",
          cityLabel = "City",
          zipRequired = required,
          cityRequired = required,
          zipValueSignal = addressSignal.map(_.zipCode.getOrElse("")),
          cityValueSignal = addressSignal.map(_.city.getOrElse("")),
          onZipChange = { value =>
            val currentAddress = addressVar.now()
            onAddressChange(currentAddress.copy(zipCode = if value.isEmpty then None else Some(value)))
          },
          onCityChange = { value =>
            val currentAddress = addressVar.now()
            onAddressChange(currentAddress.copy(city = if value.isEmpty then None else Some(value)))
          }
        )
      else emptyNode,

      // Country Field
      if showCountry then
        div(
          className := "form-field",
          Label(
            display := "block",
            marginBottom := "0.25rem",
            fontWeight := "600",
            "Country",
            if required then
              span(
                color := "red",
                marginLeft := "0.25rem",
                "*"
              )
            else emptyNode
          ),
          Input(
            _.placeholder := "e.g., Schweiz",
            _.value <-- addressSignal.map(_.country.getOrElse("")),
            _.events.onChange.mapToValue --> Observer[String] { value =>
              val currentAddress = addressVar.now()
              onAddressChange(currentAddress.copy(country = if value.isEmpty then None else Some(value)))
            }
          )
        )
      else emptyNode
    )

end AddressField

