package pme123.geak4s.domain.project

import pme123.geak4s.domain.Address

/** Auftraggeber - Client information */
case class Client(
  salutation: Anrede,      // Anrede
  name1: Option[String],            // Name 1
  name2: Option[String],            // Name 2
  address: Address,                 // Address (street, houseNumber, zipCode, city, country)
  poBox: Option[String],            // Postfach
  email: Option[String],            // E-Mail
  phone1: Option[String],           // Telefon 1
  phone2: Option[String]            // Telefon 2
)

object Client:
  lazy val example: Client = Client(
    salutation = Anrede.Herr,
    name1 = Some("Max Mustermann"),
    name2 = Some("Firma AG"),
    address = Address.example,
    poBox = Some("123b"),
    email = Some("max.mustermann@example.com"),
    phone1 = Some("+41 44 123 45 67"),
    phone2 = Some("+41 79 987 65 43")
  )
end Client

