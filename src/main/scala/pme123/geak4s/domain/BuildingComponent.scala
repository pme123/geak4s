package pme123.geak4s.domain

/** Building component with thermal conductivity */
case class BuildingComponent(
  name: String,
  thermalConductivity: Double  // λ [W/(m·K)] - Lambda value
)

/** Catalog of building components with their thermal conductivity values */
object BuildingComponentCatalog:
  
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
  
end BuildingComponentCatalog

