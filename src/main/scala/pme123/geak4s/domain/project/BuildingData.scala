package pme123.geak4s.domain.project

/** Building data */
case class BuildingData(
  constructionType: Option[String],
  groundPlanType: Option[String],
  constructionYear: Option[Int],
  renovationYear: Option[Int],
  numberOfFloors: Option[Int],
  numberOfApartments: Option[Int],
  energyReferenceArea: Option[Double],
  thermalEnvelopeArea: Option[Double]
)

object BuildingData:
  lazy val example: BuildingData = BuildingData(
    constructionType = Some("Massivbau"),
    groundPlanType = Some("kompakt"),
    constructionYear = Some(1975),
    renovationYear = Some(2010),
    numberOfFloors = Some(4),
    numberOfApartments = Some(12),
    energyReferenceArea = Some(850.5),
    thermalEnvelopeArea = Some(1250.8)
  )
end BuildingData

