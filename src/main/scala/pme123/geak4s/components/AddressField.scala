package pme123.geak4s.components

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*
import pme123.geak4s.domain.Address
import pme123.geak4s.services.{GeoAdminService, AddressResult}
import scala.concurrent.ExecutionContext.Implicits.global
import pme123.geak4s.validation.Validators

/** Reusable Address field component */
object AddressField:

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

    div(
      className := "address-field",

      // Update the Var when the signal changes
      addressSignal --> addressVar.writer,

      // Section Label with Search Field
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
            width := "100%",
            _.events.onInput.mapToValue --> Observer[String] { searchValue =>
              // Cancel any pending search timer
              searchTimer.foreach(org.scalajs.dom.window.clearTimeout)

              if searchValue.trim.isEmpty then
                showSearchSuggestions.set(false)
              else
                // Wait 1 second before searching
                searchTimer = Some(org.scalajs.dom.window.setTimeout(() => {
                  GeoAdminService.searchAddress(searchValue).foreach { results =>
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
                    onMouseEnter --> Observer[org.scalajs.dom.MouseEvent] { e =>
                      e.target.asInstanceOf[org.scalajs.dom.HTMLElement].style.backgroundColor = "#f5f5f5"
                    },
                    onMouseLeave --> Observer[org.scalajs.dom.MouseEvent] { e =>
                      e.target.asInstanceOf[org.scalajs.dom.HTMLElement].style.backgroundColor = "white"
                    },
                    onMouseDown --> Observer[org.scalajs.dom.MouseEvent] { e =>
                      e.preventDefault() // Prevent input blur
                      val address = GeoAdminService.toAddress(result)
                      onAddressChange(address)
                      showSearchSuggestions.set(false)
                    },
                    // Remove <b> and </b> tags from label
                    result.label.replace("<b>", "").replace("</b>", "")
                  )
                }
              )
            case _ => emptyNode
          }
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

