package pme123.geak4s.domain.project

import pme123.geak4s.domain.Address

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
        address = Address(
          street = Some("Nebenstrasse"),
          houseNumber = Some("45"),
          zipCode = Some("8000"),
          city = Some("Zürich"),
          country = Some("Schweiz"),
          lat = Some(47.3769),  // Zürich coordinates
          lon = Some(8.5417)
        )
      )
    )
  )
end EgidEdidGroup

case class EgidEdidEntry(
  egid: Option[String],   // EGID
  edid: Option[String],   // EDID
  address: Address        // Adresse
)

object EgidEdidEntry:
  lazy val example: EgidEdidEntry = EgidEdidEntry(
    egid = Some("123456"),
    edid = Some("1"),
    address = Address(
      street = Some("Musterstrasse"),
      houseNumber = Some("123"),
      zipCode = Some("8000"),
      city = Some("Zürich"),
      country = Some("Schweiz"),
      lat = Some(47.3769),  // Zürich coordinates
      lon = Some(8.5417)
    )
  )
end EgidEdidEntry

