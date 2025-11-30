package pme123.geak4s.domain.project

/** Auftraggeber - Client information */
case class Client(
  salutation: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  address: Option[String],
  poBox: Option[String],
  zipCode: Option[String],
  city: Option[String],
  phone: Option[String],
  email: Option[String]
)

object Client:
  lazy val example: Client = Client(
    salutation = Some("Herr"),
    firstName = Some("Max"),
    lastName = Some("Mustermann"),
    address = Some("Musterstrasse 123"),
    poBox = None,
    zipCode = Some("8000"),
    city = Some("Zürich"),
    phone = Some("+41 44 123 45 67"),
    email = Some("max.mustermann@example.com")
  )
end Client

