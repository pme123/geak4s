package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*
import pme123.geak4s.domain.project.*
import pme123.geak4s.domain.{Address, FieldMetadata}
import pme123.geak4s.state.AppState
import pme123.geak4s.components.{AddressField, FormField}
import pme123.geak4s.validation.{Validators, ValidationResult}

case class ProjectView(project: Project):

  def render(): HtmlElement =
    div(
      className := "project-view",

      // Projekt Section
      renderSection(
        title = "Projekt",
        content = renderProjectSection()
      ),

      // Auftraggeber Section
      renderSection(
        title = "Auftraggeber",
        content = renderClientSection(project.client)
      ),

      // Gebäude Section
      renderSection(
        title = "Gebäude",
        content = renderBuildingSection()
      ),

      // Gebäudedaten Section (Enhanced with FieldMetadata)
      BuildingDataView(project.buildingData),

      // Beschreibungen Section
      renderSection(
        title = "Beschreibungen im Ist-Zustand",
        content = renderDescriptionsSection()
      ),

      // EGID_EDID-Gruppe Section
      renderSection(
        title = "EGID_EDID-Gruppe",
        content = renderEgidEdidSection()
      )
    )
  
  private def renderSection(title: String, content: HtmlElement): HtmlElement =
    div(
      className := "form-section",
      div(
        className := "section-header",
        Title(
          _.level := TitleLevel.H3,
          title
        )
      ),
      content
    )
  
  private def renderProjectSection(): HtmlElement =
    div(
      formField(
        label = "Projektbezeichnung",
        required = true,
        placeholder = "e.g., Testobjekt Zaida",
        value = project.projectName,
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(projectName = value)
        ))
      )
    )
  
  private def renderClientSection(client: Client): HtmlElement =
    div(
      // Salutation - Enhanced with FormField
      FormField(
        metadata = FieldMetadata.salutation,
        value = AppState.projectSignal.map(_.map(_.project.client.salutation.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(salutation = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      // Name 1 - Enhanced with FormField
      FormField(
        metadata = FieldMetadata.clientName1,
        value = AppState.projectSignal.map(_.map(_.project.client.name1.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(name1 = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      // Name 2 - Enhanced with FormField
      FormField(
        metadata = FieldMetadata.clientName2,
        value = AppState.projectSignal.map(_.map(_.project.client.name2.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(name2 = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      // Address Field Component
      AddressField(
        label = "Address",
        required = false,
        addressSignal = AppState.projectSignal.map(_.map(_.project.client.address).getOrElse(Address.empty)),
        onAddressChange = address => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(address = address)
          )
        ))
      ),

      formField(
        label = "Postfach",
        required = false,
        placeholder = "e.g., Postfach 456",
        value = client.poBox.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(poBox = if value.isEmpty then None else Some(value))
          )
        ))
      ),
      
      formField(
        label = "E-Mail",
        required = false,
        placeholder = "e.g., max.mustermann@example.com",
        value = client.email.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(email = if value.isEmpty then None else Some(value))
          )
        )),
        validator = Validators.email
      ),

      formField(
        label = "Telefon 1",
        required = false,
        placeholder = "e.g., +41 44 123 45 67",
        value = client.phone1.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(phone1 = if value.isEmpty then None else Some(value))
          )
        )),
        validator = Validators.phone
      ),

      formField(
        label = "Telefon 2",
        required = false,
        placeholder = "e.g., +41 44 987 65 43",
        value = client.phone2.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(phone2 = if value.isEmpty then None else Some(value))
          )
        )),
        validator = Validators.phone
      )
    )
  
  private def renderBuildingSection(): HtmlElement =
    // Check if building address matches client address
    val addressesMatch = project.buildingLocation.address == project.client.address
    val useSameAddressVar = Var(addressesMatch)

    div(
      // Checkbox to use client address
      div(
        className := "form-field",
        marginBottom := "1rem",
        CheckBox(
          _.text := "Adresse Auftraggeber",
          _.checked <-- useSameAddressVar.signal,
          _.events.onChange.mapToChecked --> Observer[Boolean] { checked =>
            useSameAddressVar.set(checked)
            if checked then
              // Copy client address to building location
              AppState.projectState.now() match
                case AppState.ProjectState.Loaded(project, filename) =>
                  AppState.updateProject(p => p.copy(
                    project = p.project.copy(
                      buildingLocation = p.project.buildingLocation.copy(address = p.project.client.address)
                    )
                  ))
                case _ => ()
          }
        )
      ),

      // Building Location - Address Field Component (disabled when using client address)
      child <-- useSameAddressVar.signal.map { useSameAddress =>
        if useSameAddress then
          // Show read-only address from client
          div(
            className := "address-field",
            Label(
              display := "block",
              marginBottom := "0.5rem",
              fontWeight := "600",
              fontSize := "1rem",
              "Building Address (from Client)"
            ),
            div(
              className := "form-row",
              div(
                className := "form-field",
                Label(
                  display := "block",
                  marginBottom := "0.25rem",
                  fontWeight := "600",
                  "Street"
                ),
                Input(
                  _.disabled := true,
                  _.value <-- AppState.projectSignal.map(_.map(_.project.client.address.street.getOrElse("")).getOrElse(""))
                )
              ),
              div(
                className := "form-field",
                Label(
                  display := "block",
                  marginBottom := "0.25rem",
                  fontWeight := "600",
                  "House Number"
                ),
                Input(
                  _.disabled := true,
                  _.value <-- AppState.projectSignal.map(_.map(_.project.client.address.houseNumber.getOrElse("")).getOrElse(""))
                )
              )
            ),
            div(
              className := "form-row",
              div(
                className := "form-field",
                Label(
                  display := "block",
                  marginBottom := "0.25rem",
                  fontWeight := "600",
                  "ZIP Code"
                ),
                Input(
                  _.disabled := true,
                  _.value <-- AppState.projectSignal.map(_.map(_.project.client.address.zipCode.getOrElse("")).getOrElse(""))
                )
              ),
              div(
                className := "form-field",
                Label(
                  display := "block",
                  marginBottom := "0.25rem",
                  fontWeight := "600",
                  "City"
                ),
                Input(
                  _.disabled := true,
                  _.value <-- AppState.projectSignal.map(_.map(_.project.client.address.city.getOrElse("")).getOrElse(""))
                )
              )
            ),
            div(
              className := "form-field",
              Label(
                display := "block",
                marginBottom := "0.25rem",
                fontWeight := "600",
                "Country"
              ),
              Input(
                _.disabled := true,
                _.value <-- AppState.projectSignal.map(_.map(_.project.client.address.country.getOrElse("")).getOrElse(""))
              )
            )
          )
        else
          // Show editable address field
          AddressField(
            label = "Building Address",
            required = false,
            addressSignal = AppState.projectSignal.map(_.map(_.project.buildingLocation.address).getOrElse(Address.empty)),
            onAddressChange = address => AppState.updateProject(p => p.copy(
              project = p.project.copy(
                buildingLocation = p.project.buildingLocation.copy(address = address)
              )
            ))
          )
      },

      formField(
        label = "Gemeinde",
        required = false,
        placeholder = "e.g., Zürich",
        value = project.buildingLocation.municipality.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingLocation = p.project.buildingLocation.copy(municipality = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      formField(
        label = "Gebäudebezeichnung",
        required = false,
        placeholder = "e.g., Wohnhaus Musterstrasse",
        value = project.buildingLocation.buildingName.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingLocation = p.project.buildingLocation.copy(buildingName = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      formField(
        label = "Parzellen-Nummer",
        required = false,
        placeholder = "e.g., 1234",
        value = project.buildingLocation.parcelNumber.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingLocation = p.project.buildingLocation.copy(parcelNumber = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      // Building Data
      div(
        className := "form-row",
        formField(
          label = "Baujahr",
          required = false,
          placeholder = "e.g., 1975",
          value = project.buildingData.constructionYear.map(_.toString).getOrElse(""),
          onChange = value => AppState.updateProject(p => p.copy(
            project = p.project.copy(
              buildingData = p.project.buildingData.copy(constructionYear = if value.isEmpty then None else value.toIntOption)
            )
          ))
        ),

        formField(
          label = "Jahr der letzten Gesamtsanierung",
          required = false,
          placeholder = "e.g., 2010",
          value = project.buildingData.lastRenovationYear.map(_.toString).getOrElse(""),
          onChange = value => AppState.updateProject(p => p.copy(
            project = p.project.copy(
              buildingData = p.project.buildingData.copy(lastRenovationYear = if value.isEmpty then None else value.toIntOption)
            )
          ))
        )
      ),

      formField(
        label = "Klimastation",
        required = false,
        placeholder = "e.g., Zürich-Fluntern",
        value = project.buildingData.weatherStation.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(weatherStation = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      formField(
        label = "Bestbekannte Werte Klimastation",
        required = false,
        placeholder = "e.g., Standard",
        value = project.buildingData.weatherStationValues.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(weatherStationValues = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      formField(
        label = "Höhe ü. M.",
        required = false,
        placeholder = "e.g., 556.0",
        value = project.buildingData.altitude.map(_.toString).getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(altitude = if value.isEmpty then None else value.toDoubleOption)
          )
        )),
        validator = Validators.decimal
      ),

      formField(
        label = "Energiebezugsfläche [m²]",
        required = false,
        placeholder = "e.g., 850.5",
        value = project.buildingData.energyReferenceArea.map(_.toString).getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(energyReferenceArea = if value.isEmpty then None else value.toDoubleOption)
          )
        )),
        validator = Validators.positive
      ),

      formField(
        label = "Lichte Raumhöhe [m]",
        required = false,
        placeholder = "e.g., 2.6",
        value = project.buildingData.clearRoomHeight.map(_.toString).getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(clearRoomHeight = if value.isEmpty then None else value.toDoubleOption)
          )
        )),
        validator = Validators.positive
      ),

      formField(
        label = "Anzahl der Vollgeschosse",
        required = false,
        placeholder = "e.g., 4",
        value = project.buildingData.numberOfFloors.map(_.toString).getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(numberOfFloors = if value.isEmpty then None else value.toIntOption)
          )
        )),
        validator = Validators.combine(Validators.integer, Validators.positive)
      ),

      formField(
        label = "Gebäudebreite [m]",
        required = false,
        placeholder = "e.g., 12.5",
        value = project.buildingData.buildingWidth.map(_.toString).getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(buildingWidth = if value.isEmpty then None else value.toDoubleOption)
          )
        ))
      ),

      formField(
        label = "Bauweise Gebäude",
        required = false,
        placeholder = "e.g., Massivbau",
        value = project.buildingData.constructionType.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(constructionType = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      formField(
        label = "Grundrisstyp",
        required = false,
        placeholder = "e.g., kompakt",
        value = project.buildingData.groundPlanType.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(groundPlanType = if value.isEmpty then None else Some(value))
          )
        ))
      )
    )

  private def renderDescriptionsSection(): HtmlElement =
    div(
      formTextArea(
        label = "Beschreibung des Gebäudes",
        required = false,
        placeholder = "e.g., Mehrfamilienhaus aus den 1970er Jahren...",
        value = project.descriptions.buildingDescription.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            descriptions = p.project.descriptions.copy(buildingDescription = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      formTextArea(
        label = "Beschreibung der Gebäudehülle",
        required = false,
        placeholder = "e.g., Ungedämmte Aussenwände, teilweise sanierte Fenster...",
        value = project.descriptions.envelopeDescription.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            descriptions = p.project.descriptions.copy(envelopeDescription = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      formTextArea(
        label = "Beschreibung Gebäudetechnik",
        required = false,
        placeholder = "e.g., Gasheizung mit Radiatoren, keine kontrollierte Lüftung...",
        value = project.descriptions.hvacDescription.getOrElse(""),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            descriptions = p.project.descriptions.copy(hvacDescription = if value.isEmpty then None else Some(value))
          )
        ))
      )
    )

  private def renderEgidEdidSection(): HtmlElement =
    div(
      project.egidEdidGroup.entries.zipWithIndex.map { case (entry, idx) =>
        div(
          className := "egid-entry",
          marginBottom := "1.5rem",
          paddingBottom := "1rem",
          borderBottom := "1px solid #e0e0e0",

          Title(
            _.level := TitleLevel.H5,
            s"Eintrag ${idx + 1}"
          ),

          formField(
            label = "EGID",
            required = false,
            placeholder = "e.g., 123456",
            value = entry.egid.getOrElse(""),
            onChange = value => updateEgidEntry(idx, entry.copy(egid = if value.isEmpty then None else Some(value)))
          ),

          formField(
            label = "EDID",
            required = false,
            placeholder = "e.g., 1",
            value = entry.edid.getOrElse(""),
            onChange = value => updateEgidEntry(idx, entry.copy(edid = if value.isEmpty then None else Some(value)))
          ),

          // Address Field with all components
          AddressField(
            label = "Adresse",
            required = false,
            addressSignal = AppState.projectSignal.map(_.map(_.project.egidEdidGroup.entries.lift(idx).map(_.address).getOrElse(Address.empty)).getOrElse(Address.empty)),
            onAddressChange = address => updateEgidEntry(idx, entry.copy(address = address)),
            showStreet = true,
            showHouseNumber = true,
            showZipCity = true,
            showCountry = false
          )
        )
      }
    )

  private def updateEgidEntry(idx: Int, newEntry: pme123.geak4s.domain.project.EgidEdidEntry): Unit =
    AppState.updateProject { p =>
      val updatedEntries = p.project.egidEdidGroup.entries.updated(idx, newEntry)
      p.copy(
        project = p.project.copy(
          egidEdidGroup = p.project.egidEdidGroup.copy(entries = updatedEntries)
        )
      )
    }

  private def formSelect(
    label: String,
    required: Boolean,
    value: String,
    options: List[String],
    onChange: String => Unit
  ): HtmlElement =
    div(
      className := "form-field",

      Label(
        display := "block",
        marginBottom := "0.25rem",
        fontWeight := "600",
        label,
        if required then
          span(
            color := "red",
            marginLeft := "0.25rem",
            "*"
          )
        else emptyNode
      ),

      select(
        cls := "form-select",
        com.raquo.laminar.api.L.required := required,
        defaultValue := value,
        com.raquo.laminar.api.L.onChange.mapToValue --> Observer[String](onChange),

        option(
          com.raquo.laminar.api.L.value := "",
          com.raquo.laminar.api.L.selected := value.isEmpty,
          "-- Please select --"
        ),

        options.map { opt =>
          option(
            com.raquo.laminar.api.L.value := opt,
            com.raquo.laminar.api.L.selected := (value == opt),
            opt
          )
        }
      )
    )

  private def formField(
    label: String,
    required: Boolean,
    placeholder: String,
    value: String,
    onChange: String => Unit,
    validator: String => ValidationResult = _ => ValidationResult.Valid
  ): HtmlElement =
    val errorMessage = Var[Option[String]](None)
    val valueState = Var[ValueState](ValueState.None)

    div(
      className := "form-field",
      marginBottom := "1rem",
      Label(
        display := "block",
        marginBottom := "0.25rem",
        fontWeight := "600",
        label,
        if required then
          span(
            color := "red",
            marginLeft := "0.25rem",
            "*"
          )
        else emptyNode
      ),
      Input(
        _.value := value,
        _.placeholder := placeholder,
        _.required := required,
        _.valueState <-- valueState.signal,
     //   _.events.onInput.mapToValue --> Observer[String](onChange),
        onBlur.mapToValue --> Observer[String] { currentValue =>
          // Validate on change (triggered when field loses focus)
          val validationResult = if required then
            Validators.combine(Validators.required, validator)(currentValue)
          else
            validator(currentValue)

          validationResult match
            case ValidationResult.Valid =>
              errorMessage.set(None)
              valueState.set(ValueState.None)
            case ValidationResult.Invalid(msg) =>
              errorMessage.set(Some(msg))
              valueState.set(ValueState.Error)
        }
      ),
      child <-- errorMessage.signal.map {
        case Some(msg) =>
          div(
            color := "#d32f2f",
            fontSize := "0.75rem",
            marginTop := "0.25rem",
            msg
          )
        case None => emptyNode
      }
    )

  private def formTextArea(
    label: String,
    required: Boolean,
    placeholder: String,
    value: String,
    onChange: String => Unit,
    validator: String => ValidationResult = _ => ValidationResult.Valid
  ): HtmlElement =
    val errorMessage = Var[Option[String]](None)
    val valueState = Var[ValueState](ValueState.None)

    div(
      className := "form-field",
      marginBottom := "1rem",
      Label(
        display := "block",
        marginBottom := "0.25rem",
        fontWeight := "600",
        label,
        if required then
          span(
            color := "red",
            marginLeft := "0.25rem",
            "*"
          )
        else emptyNode
      ),
      TextArea(
        _.value := value,
        _.placeholder := placeholder,
        _.required := required,
        _.rows := 4,
        _.valueState <-- valueState.signal,
      //  _.events.onInput.mapToValue --> Observer[String](onChange),
        onBlur.mapToValue --> Observer[String] { currentValue =>
          // Validate on change (triggered when field loses focus)
          val validationResult = if required then
            Validators.combine(Validators.required, validator)(currentValue)
          else
            validator(currentValue)

          validationResult match
            case ValidationResult.Valid =>
              errorMessage.set(None)
              valueState.set(ValueState.None)
            case ValidationResult.Invalid(msg) =>
              errorMessage.set(Some(msg))
              valueState.set(ValueState.Error)
        }
      ),
      child <-- errorMessage.signal.map {
        case Some(msg) =>
          div(
            color := "#d32f2f",
            fontSize := "0.75rem",
            marginTop := "0.25rem",
            msg
          )
        case None => emptyNode
      }
    )
end ProjectView

