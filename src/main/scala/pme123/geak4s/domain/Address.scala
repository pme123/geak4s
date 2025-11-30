package pme123.geak4s.domain

/** Address - Reusable address component */
case class Address(
  street: Option[String],
  houseNumber: Option[String],
  zipCode: Option[String],
  city: Option[String],
  country: Option[String]
)

object Address:
  lazy val empty: Address = Address(
    street = None,
    houseNumber = None,
    zipCode = None,
    city = None,
    country = None
  )

  lazy val example: Address = Address(
    street = Some("Musterstrasse"),
    houseNumber = Some("123"),
    zipCode = Some("8000"),
    city = Some("Zürich"),
    country = Some("Schweiz")
  )
end Address

