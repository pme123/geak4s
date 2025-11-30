package pme123.geak4s.domain.building

/** Gebäudenutzungen - Building usage */
case class BuildingUsage(
  usageType: String,
  usageSubType: Option[String],
  area: Double,
  areaPercentage: Option[Double],
  constructionYear: Option[Int]
)

object BuildingUsage:
  lazy val example: BuildingUsage = BuildingUsage(
    usageType = "Mehrfamilienhaus",
    usageSubType = Some("Wohnung"),
    area = 850.5,
    areaPercentage = Some(100.0),
    constructionYear = Some(1975)
  )
  
  lazy val exampleMixed: List[BuildingUsage] = List(
    BuildingUsage(
      usageType = "Mehrfamilienhaus",
      usageSubType = Some("Wohnung"),
      area = 650.0,
      areaPercentage = Some(75.0),
      constructionYear = Some(1975)
    ),
    BuildingUsage(
      usageType = "Büro/Verwaltung",
      usageSubType = Some("Büro"),
      area = 200.5,
      areaPercentage = Some(25.0),
      constructionYear = Some(2010)
    )
  )
end BuildingUsage

