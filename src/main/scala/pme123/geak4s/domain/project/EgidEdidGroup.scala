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
        egid = "987654",
        edid = Some("2"),
        address = "Nebengebäude",
        zipCity = "8000 Zürich"
      )
    )
  )
end EgidEdidGroup

case class EgidEdidEntry(
  egid: String,
  edid: Option[String],
  address: String,
  zipCity: String
)

object EgidEdidEntry:
  lazy val example: EgidEdidEntry = EgidEdidEntry(
    egid = "123456",
    edid = Some("1"),
    address = "Hauptgebäude, Musterstrasse 123",
    zipCity = "8000 Zürich"
  )
end EgidEdidEntry

