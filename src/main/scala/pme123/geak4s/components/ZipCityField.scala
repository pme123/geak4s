package pme123.geak4s.components

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*
import pme123.geak4s.data.SwissZipCodes
import pme123.geak4s.validation.{Validators, ValidationResult}

/** Reusable ZIP-City field component with auto-complete */
object ZipCityField:

  def apply(
    zipLabel: String = "PLZ",
    cityLabel: String = "Ort",
    zipRequired: Boolean = false,
    cityRequired: Boolean = false,
    zipValueSignal: Signal[String],
    cityValueSignal: Signal[String],
    onZipChange: String => Unit,
    onCityChange: String => Unit
  ): HtmlElement =
    val zipSuggestions = Var(List.empty[SwissZipCodes.City])
    val citySuggestions = Var(List.empty[SwissZipCodes.City])
    val showZipSuggestions = Var(false)
    val showCitySuggestions = Var(false)
    val zipErrorMessage = Var[Option[String]](None)
    val cityErrorMessage = Var[Option[String]](None)
    val zipValueState = Var[ValueState](ValueState.None)
    val cityValueState = Var[ValueState](ValueState.None)
    var zipHideTimer: Option[Int] = None
    var cityHideTimer: Option[Int] = None
    
    div(
      className := "form-row",
      position := "relative",
      
      // ZIP Code Field
      div(
        className := "form-field",
        flex := "1",
        position := "relative",
        
        Label(
          display := "block",
          marginBottom := "0.25rem",
          fontWeight := "600",
          zipLabel,
          if zipRequired then
            span(
              color := "red",
              marginLeft := "0.25rem",
              "*"
            )
          else emptyNode
        ),
        
        Input(
          _.value <-- zipValueSignal,
          _.placeholder := "e.g., 6414",
          _.required := zipRequired,
          _.valueState <-- zipValueState.signal,
          _.events.onInput.mapToValue --> Observer[String] { value =>
            onZipChange(value)

            // Cancel any pending hide timer
            zipHideTimer.foreach(org.scalajs.dom.window.clearTimeout)

            // Auto-complete: search for matching ZIP codes
            if value.nonEmpty then
              val matches = SwissZipCodes.searchByZip(value)
              zipSuggestions.set(matches)
              showZipSuggestions.set(matches.nonEmpty)

              // Auto-fill city if exact match
              SwissZipCodes.findCityByZip(value).foreach { city =>
                onCityChange(city)
                showZipSuggestions.set(false)
              }
            else
              showZipSuggestions.set(false)
          },
          _.events.onChange.mapToValue --> Observer[String] { value =>
            // Validate on change (triggered when field loses focus)
            val validationResult = if zipRequired then
              Validators.combine(Validators.required, Validators.swissZip)(value)
            else
              Validators.swissZip(value)

            validationResult match
              case ValidationResult.Valid =>
                zipErrorMessage.set(None)
                zipValueState.set(ValueState.None)
              case ValidationResult.Invalid(msg) =>
                zipErrorMessage.set(Some(msg))
                zipValueState.set(ValueState.Error)
          }
        ),

        // ZIP Error Message
        child <-- zipErrorMessage.signal.map {
          case Some(msg) =>
            div(
              color := "#d32f2f",
              fontSize := "0.75rem",
              marginTop := "0.25rem",
              marginBottom := "0.5rem",
              msg
            )
          case None => emptyNode
        },
        
        // ZIP Suggestions Dropdown
        child <-- showZipSuggestions.signal.combineWith(zipSuggestions.signal).map {
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
              maxHeight := "200px",
              overflowY := "auto",
              zIndex := "1000",
              marginTop := "2px",
              
              suggestions.map { zipCity =>
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
                    onZipChange(zipCity.zip)
                    onCityChange(zipCity.name)
                    showZipSuggestions.set(false)
                  },
                  s"${zipCity.zip} - ${zipCity.name} (${zipCity.canton})"
                )
              }
            )
          case _ => emptyNode
        }
      ),
      
      // City Field
      div(
        className := "form-field",
        flex := "1",
        position := "relative",
        marginLeft := "1rem",
        
        Label(
          display := "block",
          marginBottom := "0.25rem",
          fontWeight := "600",
          cityLabel,
          if cityRequired then
            span(
              color := "red",
              marginLeft := "0.25rem",
              "*"
            )
          else emptyNode
        ),
        
        Input(
          _.value <-- cityValueSignal,
          _.placeholder := "e.g., Oberarth",
          _.required := cityRequired,
          _.valueState <-- cityValueState.signal,
          _.events.onInput.mapToValue --> Observer[String] { value =>
            onCityChange(value)

            // Cancel any pending hide timer
            cityHideTimer.foreach(org.scalajs.dom.window.clearTimeout)

            // Auto-complete: search for matching cities
            if value.length >= 2 then
              val matches = SwissZipCodes.searchByCity(value)
              citySuggestions.set(matches)
              showCitySuggestions.set(matches.nonEmpty)
            else
              showCitySuggestions.set(false)
          },
          _.events.onChange.mapToValue --> Observer[String] { value =>
            // Validate on change (triggered when field loses focus)
            val validationResult = if cityRequired then
              Validators.required(value)
            else
              ValidationResult.Valid

            validationResult match
              case ValidationResult.Valid =>
                cityErrorMessage.set(None)
                cityValueState.set(ValueState.None)
              case ValidationResult.Invalid(msg) =>
                cityErrorMessage.set(Some(msg))
                cityValueState.set(ValueState.Error)
          }
        ),

        // City Error Message
        child <-- cityErrorMessage.signal.map {
          case Some(msg) =>
            div(
              color := "#d32f2f",
              fontSize := "0.75rem",
              marginTop := "0.25rem",
              marginBottom := "0.5rem",
              msg
            )
          case None => emptyNode
        },
        
        // City Suggestions Dropdown
        child <-- showCitySuggestions.signal.combineWith(citySuggestions.signal).map {
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
              maxHeight := "200px",
              overflowY := "auto",
              zIndex := "1000",
              marginTop := "2px",
              
              suggestions.map { zipCity =>
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
                    onZipChange(zipCity.zip)
                    onCityChange(zipCity.name)
                    showCitySuggestions.set(false)
                  },
                  s"${zipCity.name} - ${zipCity.zip} (${zipCity.canton})"
                )
              }
            )
          case _ => emptyNode
        }
      )
    )

end ZipCityField

