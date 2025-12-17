package pme123.geak4s.domain.uwert

case class BWert(
    name: String,
    bValue: Double,
    applicableFor: Set[ComponentType] // Component types this material can be used for
)

object BWert:
  import ComponentType.*

  val values: List[BWert] = List(
    BWert("Estrichraum, Schrägdach ungedämmt", 0.9, Set(AtticFloor, PitchedRoof)),
    BWert("Estrichraum, Schrägdach gedämmt U<0.4", 0.7, Set(AtticFloor, PitchedRoof)),
    BWert("Kellerraum ganz im Erdreich", 0.7, Set(BasementFloor, BasementWall, BasementCeiling)),
    BWert("Kellerraum teilweise oder ganz über dem Erdreich", 0.8, Set(BasementFloor, BasementWall, BasementCeiling)),
    BWert("angebauter Raum", 0.8, Set(ExteriorWall, Floor, BasementCeiling)),
    BWert("Glasvorbau", 0.9, Set(ExteriorWall, Other)),
    BWert("Kellerboden im Erdreich", 0.3, Set(BasementFloor))
  )

  /** Get b-values applicable for a specific component type */
  def getByComponentType(componentType: ComponentType): List[BWert] =
    values.filter(_.applicableFor.contains(componentType)).sortBy(_.name)

end BWert
