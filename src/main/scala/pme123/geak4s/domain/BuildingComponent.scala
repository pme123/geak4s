package pme123.geak4s.domain

/** Building component with thermal conductivity */
case class BuildingComponent(
  name: String,
  thermalConductivity: Double  // λ [W/(m·K)] - Lambda value
)

/** Catalog of building components with their thermal conductivity values */
object BuildingComponentCatalog:

  /** All available building components */
  val components: List[BuildingComponent] = List(
    BuildingComponent("Exterior Plaster", 0.87),
    BuildingComponent("Expanded Clay Concrete", 0.29),
    BuildingComponent("Expanded Clay Concrete", 0.5),
    BuildingComponent("Expanded Clay Concrete", 1.0),
    BuildingComponent("Rubble Stone (Limestone)", 1.46),
    BuildingComponent("Rubble Stone (Sandstone)", 1.34),
    BuildingComponent("Rubble Stone Masonry", 2.1),
    BuildingComponent("Beech Wood", 0.17),
    BuildingComponent("Calmo Brick", 0.7),
    BuildingComponent("Duripanel Board", 0.26),
    BuildingComponent("Durisol Roof Panel", 0.12),
    BuildingComponent("Oak Wood", 0.21),
    BuildingComponent("Single Wythe Masonry", 0.44),
    BuildingComponent("Earth Material", 2.3),
    BuildingComponent("Spruce Wood", 0.14),
    BuildingComponent("Flumroc Roof Panel", 0.04),
    BuildingComponent("Flumroc Insulation Board", 0.036),
    BuildingComponent("Flumroc Fine Granulate", 0.04),
    BuildingComponent("Foam Glass Board", 0.044),
    BuildingComponent("Aerated Concrete", 0.16),
    BuildingComponent("Aerated Concrete", 0.19),
    BuildingComponent("Aerated Concrete Roof Panel", 0.2),
    BuildingComponent("Gypsum Plasterboard", 0.21),
    BuildingComponent("Gypsum Board", 0.4),
    BuildingComponent("Glass Fibers", 0.04),
    BuildingComponent("Glass Fiber Board", 0.04),
    BuildingComponent("Glass Wool", 0.04),
    BuildingComponent("Mastic Asphalt Coating", 0.7),
    BuildingComponent("Chipboard", 0.12),
    BuildingComponent("Chipboard Semi-Hard", 0.085),
    BuildingComponent("Chipboard Hard", 0.17),
    BuildingComponent("Chipboard Novophen", 0.12),
    BuildingComponent("Chipboard Soft", 0.06),
    BuildingComponent("Interior Plaster", 0.7),
    BuildingComponent("Insulating Brick", 0.47),
    BuildingComponent("Calcium Silicate Brick", 1.0),
    BuildingComponent("Ceramic Tiles", 1.0),
    BuildingComponent("Gravel/Sand Protection Layer", 1.8),
    BuildingComponent("Clinker Tiles", 1.0),
    BuildingComponent("Coconut Fiber Mat", 0.05),
    BuildingComponent("Cork Board", 0.042),
    BuildingComponent("Cork Granulate Expanded", 0.042),
    BuildingComponent("Cork Granulate Natural", 0.06),
    BuildingComponent("Cork Granulate Mat", 0.046),
    BuildingComponent("Marble Slabs", 2.3),
    BuildingComponent("Perlite Board", 0.06),
    BuildingComponent("Perlite Fill", 0.07),
    BuildingComponent("Polystyrene Board Expanded", 0.038),
    BuildingComponent("Polystyrene Board Extruded", 0.034),
    BuildingComponent("Polyurethane Rigid Foam", 0.03),
    BuildingComponent("Round Gravel", 2.3),
    BuildingComponent("Sarna Exterior Insulation Board", 0.038),
    BuildingComponent("Sarnapur Board", 0.03),
    BuildingComponent("Sarnatherm Polystyrene Board", 0.036),
    BuildingComponent("Foam Glass Board", 0.048),
    BuildingComponent("Foam Polystyrene Expanded", 0.038),
    BuildingComponent("Face Brick", 0.52),
    BuildingComponent("Chippings", 1.5),
    BuildingComponent("Reinforced Concrete", 1.8),
    BuildingComponent("Mineral Wool", 0.04),
    BuildingComponent("Mineral Wool Board", 0.04),
    BuildingComponent("Stoneware Tiles", 1.5),
    BuildingComponent("Clay Insulation Board", 0.44),
    BuildingComponent("Clay Tiles", 1.0),
    BuildingComponent("Composite Masonry", 0.37),
    BuildingComponent("Cement-Bonded Wood Wool Board", 0.11),
    BuildingComponent("Cement Coating", 1.5),
    BuildingComponent("Wood Wool Board", 0.09)
  )

  /** Get thermal conductivity by component name */
  def getThermalConductivityByName(name: String): Option[Double] =
    components.find(_.name.equalsIgnoreCase(name)).map(_.thermalConductivity)

  /** Get all component names for autocomplete */
  def getAllNames: List[String] =
    components.map(_.name).distinct.sorted

  /** Search components by partial name match */
  def searchByName(partial: String): List[BuildingComponent] =
    if partial.isEmpty then List.empty
    else components.filter(_.name.toLowerCase.contains(partial.toLowerCase)).distinct

  /** Get components grouped by thermal conductivity range */
  def getByThermalConductivityRange(min: Double, max: Double): List[BuildingComponent] =
    components.filter(c => c.thermalConductivity >= min && c.thermalConductivity <= max)

  /** Get all unique thermal conductivity values */
  def getAllThermalConductivities: List[Double] =
    components.map(_.thermalConductivity).distinct.sorted

end BuildingComponentCatalog

