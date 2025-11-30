package pme123.geak4s.domain.project

/** Expert information - GEAK Expert */
case class Expert(
  salutation: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  company: Option[String],
  address: Option[String],
  zipCode: Option[String],
  city: Option[String],
  phone: Option[String],
  email: Option[String]
)

object Expert:
  lazy val example: Expert = Expert(
    salutation = Some("Frau"),
    firstName = Some("Anna"),
    lastName = Some("Expertin"),
    company = Some("GEAK Beratung AG"),
    address = Some("Energieweg 42"),
    zipCode = Some("3000"),
    city = Some("Bern"),
    phone = Some("+41 31 987 65 43"),
    email = Some("anna.expertin@geak-beratung.ch")
  )
end Expert

