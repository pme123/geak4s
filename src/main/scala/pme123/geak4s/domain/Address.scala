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
):

  def asCopyString: String =
    s"${street.mkString} ${houseNumber.mkString} ${zipCode.mkString} ${city.mkString} ${country.mkString}"

  def coordString: String  = s"${lat.getOrElse("-")}, ${lon.getOrElse("-")}"
end Address

object Address:
  lazy val empty: Address = Address(
    street = None,
    houseNumber = None,
    zipCode = None,
    city = None,
    country = None
  )

  lazy val example: Address = Address(
    street = Some("Guyer-Zeller-Strasse"),
    houseNumber = Some("27"),
    zipCode = Some("8620"),
    city = Some("Wetzikon ZH"),
    country = Some("Schweiz"),
    lat = Some(2702270.29),
    lon = Some(1241619.77)
  )
end Address
