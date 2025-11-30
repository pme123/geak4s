package pme123.geak4s.domain.project

/** Beschreibungen im Ist-Zustand - Descriptions of current state */
case class Descriptions(
  buildingDescription: Option[String],      // Beschreibung des Gebäudes
  envelopeDescription: Option[String],      // Beschreibung der Gebäudehülle
  hvacDescription: Option[String]           // Beschreibung Gebäudetechnik
)

object Descriptions:
  lazy val example: Descriptions = Descriptions(
    buildingDescription = Some("Mehrfamilienhaus aus den 1970er Jahren mit 4 Vollgeschossen"),
    envelopeDescription = Some("Ungedämmte Aussenwände, teilweise sanierte Fenster"),
    hvacDescription = Some("Gasheizung mit Radiatoren, keine kontrollierte Lüftung")
  )
end Descriptions

