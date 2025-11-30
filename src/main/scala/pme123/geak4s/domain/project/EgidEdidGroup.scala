package pme123.geak4s.domain.project

/** EGID_EDID-Gruppe - Building identification */
case class EgidEdidGroup(
  entries: List[EgidEdidEntry]
)

object EgidEdidGroup:
  lazy val example: EgidEdidGroup = EgidEdidGroup(
    entries = List(
      EgidEdidEntry.example,
      EgidEdidEntry(
        egid = Some("987654"),
        edid = Some("2"),
        address = Some("Nebengebäude"),
        zipCode = Some("8000"),
        city = Some("Zürich")
      )
    )
  )
end EgidEdidGroup

case class EgidEdidEntry(
  egid: Option[String],      // EGID
  edid: Option[String],      // EDID
  address: Option[String],   // Adresse
  zipCode: Option[String],   // PLZ
  city: Option[String]       // Ort
)

object EgidEdidEntry:
  lazy val example: EgidEdidEntry = EgidEdidEntry(
    egid = Some("123456"),
    edid = Some("1"),
    address = Some("Hauptgebäude, Musterstrasse 123"),
    zipCode = Some("8000"),
    city = Some("Zürich")
  )
end EgidEdidEntry

