package pme123.geak4s.components

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*
import pme123.geak4s.validation.{Validators, ValidationResult}

/** Reusable ZIP-City field component (simplified - no auto-complete) */
object ZipCityField:

  def apply(
    zipLabel: String = "PLZ",
    cityLabel: String = "Ort",
    disabled: Boolean = false,
    zipValue: String = "",
    cityValue: String = ""
  ): HtmlElement =

    div(
      className := "form-row",

      // ZIP Code Field
      div(
        className := "form-field",
        flex := "1",

        Label(
          display := "block",
          marginBottom := "0.25rem",
          fontWeight := "600",
          zipLabel
        ),

        Input(
          _.value := zipValue,
          _.placeholder := "e.g., 6414",
          _.disabled := disabled
        )
      ),

      // City Field
      div(
        className := "form-field",
        flex := "1",
        marginLeft := "1rem",

        Label(
          display := "block",
          marginBottom := "0.25rem",
          fontWeight := "600",
          cityLabel
        ),

        Input(
          _.value := cityValue,
          _.placeholder := "e.g., Oberarth",
          _.disabled := disabled
        )
      )
    )

end ZipCityField

