package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*
import pme123.geak4s.domain.project.*
import pme123.geak4s.domain.FieldMetadata
import pme123.geak4s.components.FormField
import pme123.geak4s.state.AppState

/**
 * Enhanced Building Data view using FieldMetadata and FormField components
 */
object BuildingDataView:

  def apply(buildingData: BuildingData): HtmlElement =
    div(
      className := "building-data-view",
      
      renderSection(
        title = "Gebäudedaten",
        content = renderBuildingDataFields(buildingData)
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

  private def renderBuildingDataFields(buildingData: BuildingData): HtmlElement =
    div(
      // Construction Year
      FormField(
        metadata = FieldMetadata.constructionYear,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.constructionYear.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              constructionYear = if value.isEmpty then None else value.toIntOption
            )
          )
        ))
      ),

      // Last Renovation Year
      FormField(
        metadata = FieldMetadata.lastRenovationYear,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.lastRenovationYear.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              lastRenovationYear = if value.isEmpty then None else value.toIntOption
            )
          )
        ))
      ),

      // Weather Station
      FormField(
        metadata = FieldMetadata.weatherStation,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.weatherStation.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              weatherStation = if value.isEmpty then None else Some(value)
            )
          )
        ))
      ),

      // Altitude
      FormField(
        metadata = FieldMetadata.altitude,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.altitude.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              altitude = if value.isEmpty then None else value.toDoubleOption
            )
          )
        ))
      ),

      // Energy Reference Area (EBF)
      FormField(
        metadata = FieldMetadata.energyReferenceArea,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.energyReferenceArea.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              energyReferenceArea = if value.isEmpty then None else value.toDoubleOption
            )
          )
        )),
        showValidation = Val(true) // Always show validation for required field
      ),

      // Clear Room Height
      FormField(
        metadata = FieldMetadata.clearRoomHeight,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.clearRoomHeight.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              clearRoomHeight = if value.isEmpty then None else value.toDoubleOption
            )
          )
        ))
      ),

      // Number of Floors
      FormField(
        metadata = FieldMetadata.numberOfFloors,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.numberOfFloors.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              numberOfFloors = if value.isEmpty then None else value.toIntOption
            )
          )
        ))
      ),

      // Building Width
      FormField(
        metadata = FieldMetadata.buildingWidth,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.buildingWidth.map(_.toString).getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              buildingWidth = if value.isEmpty then None else value.toDoubleOption
            )
          )
        ))
      ),

      // Construction Type
      FormField(
        metadata = FieldMetadata.constructionType,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.constructionType.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              constructionType = if value.isEmpty then None else Some(value)
            )
          )
        ))
      ),

      // Ground Plan Type
      FormField(
        metadata = FieldMetadata.groundPlanType,
        value = AppState.projectSignal.map(_.map(_.project.buildingData.groundPlanType.getOrElse("")).getOrElse("")),
        onChange = value => AppState.updateProject(p => p.copy(
          project = p.project.copy(
            buildingData = p.project.buildingData.copy(
              groundPlanType = if value.isEmpty then None else Some(value)
            )
          )
        ))
      )
    )

end BuildingDataView

