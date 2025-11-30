package pme123.geak4s.domain.project

/** Auftraggeber - Client information */
case class Client(
  salutation: Option[String],      // Anrede
  name1: Option[String],            // Name 1
  name2: Option[String],            // Name 2
  street: Option[String],               // Strasse
  houseNumber: Option[String],          // Hausnummer
  poBox: Option[String],            // Postfach
  zipCode: Option[String],          // PLZ
  city: Option[String],             // Ort
  country: Option[String],          // Land
  email: Option[String],            // E-Mail
  phone1: Option[String],           // Telefon 1
  phone2: Option[String]            // Telefon 2
)

object Client:
  lazy val example: Client = Client(
    salutation = Some("Herr"),
    name1 = Some("Max Mustermann"),
    name2 = Some("Firma AG"),
    street = Some("Musterstrasse"),
    houseNumber = Some("123"),
    poBox = None,
    zipCode = Some("8000"),
    city = Some("Zürich"),
    country = Some("Schweiz"),
    email = Some("max.mustermann@example.com"),
    phone1 = Some("+41 44 123 45 67"),
    phone2 = None
  )
end Client

