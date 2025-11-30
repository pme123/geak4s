package pme123.geak4s.domain.project

/** Projekt - Main project information */
case class Project(
  projectName: String,
  client: Client,
  egidEdidGroup: EgidEdidGroup,
  expert: Expert,
  buildingLocation: BuildingLocation,
  buildingData: BuildingData,
  templateVersion: String,
  generatedDate: String
)

object Project:
  lazy val example: Project = Project(
    projectName = "Testobjekt Zaida",
    client = Client.example,
    egidEdidGroup = EgidEdidGroup.example,
    expert = Expert.example,
    buildingLocation = BuildingLocation.example,
    buildingData = BuildingData.example,
    templateVersion = "R6.8",
    generatedDate = "2025-11-30"
  )
end Project

