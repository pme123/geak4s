package pme123.geak4s.domain.project

/** Projekt - Main project information */
case class Project(
  projectName: String,                    // Projektbezeichnung
  client: Client,                         // Auftraggeber
  buildingLocation: BuildingLocation,     // Gebäude (Location)
  buildingData: BuildingData,             // Gebäude (Technical Data)
  descriptions: Descriptions,             // Beschreibungen im Ist-Zustand
  egidEdidGroup: EgidEdidGroup,          // EGID_EDID-Gruppe
  templateVersion: String,
  generatedDate: String
)

object Project:
  lazy val example: Project = Project(
    projectName = "Testprojekt EFH Meier",
    client = Client.example,
    buildingLocation = BuildingLocation.example,
    buildingData = BuildingData.example,
    descriptions = Descriptions.example,
    egidEdidGroup = EgidEdidGroup.example,
    templateVersion = "R6.8",
    generatedDate = "2025-11-30"
  )
end Project

