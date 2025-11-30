package pme123.geak4s.domain

/** Address - Reusable address component */
case class Address(
  street: Option[String],
  houseNumber: Option[String],
  zipCode: Option[String],
  city: Option[String],
  country: Option[String],
  lat: Option[Double] = None,
  lon: Option[Double] = None
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
    street = Some("In der Halte"),
    houseNumber = Some("1"),
    zipCode = Some("6487"),
    city = Some("Göschenen"),
    country = Some("Schweiz")
  )
end Address

