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
      FormField(
        metadata = FieldMetadata.projectName,
        value = AppState.projectSignal.map(_.map(_.project.projectName).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(projectName = value)
        ))
      ),

      // Show "Start Sync" button only if sync is not initialized
      child.maybe <-- AppState.syncInitialized.signal.combineWith(
        AppState.projectSignal.map(_.map(_.project.projectName).getOrElse(""))
      ).map { (initialized, projectName) =>
        if !initialized then
          val hasProjectName = projectName.trim.nonEmpty
          Some(div(
            marginTop := "1rem",
            Button(
              _.design := ButtonDesign.Default,
              _.icon := IconName.`synchronize`,
              _.disabled := !hasProjectName,
              _.events.onClick.mapTo(()) --> Observer[Unit] { _ =>
                AppState.initializeSync()
              },
              "Sync starten"
            ),
            div(
              marginTop := "0.5rem",
              fontSize := "0.875rem",
              color := "#666",
              if hasProjectName then
                "Klicken Sie hier, um die automatische Synchronisierung mit Google Drive zu starten."
              else
                "Bitte geben Sie zuerst eine Projektbezeichnung ein, um die Synchronisierung zu starten."
            )
          ))
        else
          None
      }
    )
  
  private def renderClientSection(client: Client): HtmlElement =
    div(
      // Salutation - Enhanced with FormField
      FormField(
        metadata = FieldMetadata.salutation,
        value = AppState.projectSignal.map(_.map(_.project.client.salutation).map(_.toString).getOrElse(Anrede.Herr.toString)),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(salutation = Anrede.valueOf(value))
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
        )),
        disabledFields = true
      ),

      FormField(
        metadata = FieldMetadata.poBox,
        value = AppState.projectSignal.map(_.map(_.project.client.poBox.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(poBox = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.email,
        value = AppState.projectSignal.map(_.map(_.project.client.email.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(email = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.phone1,
        value = AppState.projectSignal.map(_.map(_.project.client.phone1.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(phone1 = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.phone2,
        value = AppState.projectSignal.map(_.map(_.project.client.phone2.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            client = p.project.client.copy(phone2 = if value.isEmpty then None else Some(value))
          )
        ))
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
            )),
            disabledFields = true
          )
      },

      FormField(
        metadata = FieldMetadata.municipality,
        value = AppState.projectSignal.map(_.map(_.project.buildingLocation.municipality.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingLocation = p.project.buildingLocation.copy(municipality = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.buildingName,
        value = AppState.projectSignal.map(_.map(_.project.buildingLocation.buildingName.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingLocation = p.project.buildingLocation.copy(buildingName = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.parcelNumber,
        value = AppState.projectSignal.map(_.map(_.project.buildingLocation.parcelNumber.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingLocation = p.project.buildingLocation.copy(parcelNumber = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      // Building Data
      div(
        className := "form-row",
        FormField(
          metadata = FieldMetadata.constructionYear,
          value = AppState.projectSignal.map(_.map(_.project.buildingData.constructionYear.map(_.toString).getOrElse("")).getOrElse("")),
          onChange = value => AppState.updateProject(p => p.copy(
            project = p.project.copy(
              buildingData = p.project.buildingData.copy(constructionYear = if value.isEmpty then None else value.toIntOption)
            )
          ))
        ),

        FormField(
          metadata = FieldMetadata.lastRenovationYear,
          value = AppState.projectSignal.map(_.map(_.project.buildingData.lastRenovationYear.map(_.toString).getOrElse("")).getOrElse("")),
          onChange = value => AppState.updateProject(p => p.copy(
            project = p.project.copy(
              buildingData = p.project.buildingData.copy(lastRenovationYear = if value.isEmpty then None else value.toIntOption)
            )
          ))
        )
      ),

      FormField(
        metadata = FieldMetadata.weatherStation,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.weatherStation.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(weatherStation = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.weatherStationValues,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.weatherStationValues.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(weatherStationValues = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.altitude,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.altitude.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(altitude = if value.isEmpty then None else value.toDoubleOption)
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.energyReferenceArea,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.energyReferenceArea.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(energyReferenceArea = if value.isEmpty then None else value.toDoubleOption)
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.clearRoomHeight,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.clearRoomHeight.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(clearRoomHeight = if value.isEmpty then None else value.toDoubleOption)
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.numberOfFloors,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.numberOfFloors.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(numberOfFloors = if value.isEmpty then None else value.toIntOption)
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.buildingWidth,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.buildingWidth.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(buildingWidth = if value.isEmpty then None else value.toDoubleOption)
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.constructionType,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.constructionType.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(constructionType = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.groundPlanType,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.groundPlanType.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(groundPlanType = if value.isEmpty then None else Some(value))
          )
        ))
      )
    )

  private def renderDescriptionsSection(): HtmlElement =
    div(
      FormField(
        metadata = FieldMetadata.buildingDescription,
        value = AppState.projectSignal.map(_.map(_.project.descriptions.buildingDescription.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            descriptions = p.project.descriptions.copy(buildingDescription = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.envelopeDescription,
        value = AppState.projectSignal.map(_.map(_.project.descriptions.envelopeDescription.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            descriptions = p.project.descriptions.copy(envelopeDescription = if value.isEmpty then None else Some(value))
          )
        ))
      ),

      FormField(
        metadata = FieldMetadata.hvacDescription,
        value = AppState.projectSignal.map(_.map(_.project.descriptions.hvacDescription.getOrElse("")).getOrElse("")),
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

          FormField(
            metadata = FieldMetadata.egid,
            value = AppState.projectSignal.map(_.map(_.project.egidEdidGroup.entries.lift(idx).flatMap(_.egid).getOrElse("")).getOrElse("")),
            onChange = value => updateEgidEntry(idx, entry.copy(egid = if value.isEmpty then None else Some(value)))
          ),

          FormField(
            metadata = FieldMetadata.edid,
            value = AppState.projectSignal.map(_.map(_.project.egidEdidGroup.entries.lift(idx).flatMap(_.edid).getOrElse("")).getOrElse("")),
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


end ProjectView

