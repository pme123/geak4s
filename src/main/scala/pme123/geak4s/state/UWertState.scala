package pme123.geak4s.state

import com.raquo.laminar.api.L.*
import pme123.geak4s.domain.uwert.*
import pme123.geak4s.domain.GeakProject
import scala.scalajs.js

/**
 * State management for U-Wert calculations
 * Persists calculation data in the project model
 */
object UWertState:

  /** All U-Wert calculations for the current project */
  val calculations: Var[List[UWertCalculation]] = Var(List.empty)

  /** Counter for generating unique IDs */
  private var idCounter = 0

  /** Initialize state from project */
  def loadFromProject(project: GeakProject): Unit =
    calculations.set(project.uwertCalculations)
    // Reset counter to avoid ID conflicts
    idCounter = project.uwertCalculations.length

  /** Get current calculations to save to project */
  def getCalculations: List[UWertCalculation] =
    calculations.now()

  /** Add a new empty calculation */
  def addCalculation(): String =
    idCounter += 1
    val id = s"uwert-calc-$idCounter"
    val newCalc = UWertCalculation.empty(id)
    calculations.update(_ :+ newCalc)
    id

  /** Remove a calculation by ID */
  def removeCalculation(id: String): Unit =
    calculations.update(_.filterNot(_.id == id))

  /** Update a calculation */
  def updateCalculation(id: String, update: UWertCalculation => UWertCalculation): Unit =
    calculations.update { calcs =>
      calcs.map { calc =>
        if calc.id == id then update(calc) else calc
      }
    }

  /** Update component selection */
  def updateComponent(id: String, component: BuildingComponent, bWertName: Option[String]): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        componentLabel = component.label,
        componentType = component.compType,
        bWertName = bWertName,
        istCalculation = UWertTableData.fromComponent(component),
        sollCalculation = UWertTableData.fromComponent(component)
      )
    )

  /** Update b-factor for both IST and SOLL */
  def updateBFactor(id: String, bFactor: Double): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        istCalculation = calc.istCalculation.copy(bFactor = bFactor),
        sollCalculation = calc.sollCalculation.copy(bFactor = bFactor)
      )
    )

  /** Update IST table materials */
  def updateIstMaterials(id: String, materials: List[MaterialLayer]): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        istCalculation = calc.istCalculation.copy(materials = materials)
      )
    )

  /** Update SOLL table materials */
  def updateSollMaterials(id: String, materials: List[MaterialLayer]): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        sollCalculation = calc.sollCalculation.copy(materials = materials)
      )
    )

  /** Update IST b-factor */
  def updateIstBFactor(id: String, bFactor: Double): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        istCalculation = calc.istCalculation.copy(bFactor = bFactor)
      )
    )

  /** Update SOLL b-factor */
  def updateSollBFactor(id: String, bFactor: Double): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        sollCalculation = calc.sollCalculation.copy(bFactor = bFactor)
      )
    )

  /** Add a new material layer to IST table */
  def addIstMaterialLayer(id: String): Unit =
    updateCalculation(id, calc =>
      val currentMaterials = calc.istCalculation.materials
      // Find the next available number (between 2 and 8)
      val usedNumbers = currentMaterials.map(_.nr).toSet
      val nextNr = (2 to 8).find(!usedNumbers.contains(_)).getOrElse(2)

      // Insert the new layer before the last row (row 9)
      val newLayer = MaterialLayer.empty(nextNr)
      val updatedMaterials = (currentMaterials.filterNot(_.nr == 9) :+ newLayer :+ currentMaterials.find(_.nr == 9).get).sortBy(_.nr)

      calc.copy(
        istCalculation = calc.istCalculation.copy(materials = updatedMaterials)
      )
    )

  /** Add a new material layer to SOLL table */
  def addSollMaterialLayer(id: String): Unit =
    updateCalculation(id, calc =>
      val currentMaterials = calc.sollCalculation.materials
      // Find the next available number (between 2 and 8)
      val usedNumbers = currentMaterials.map(_.nr).toSet
      val nextNr = (2 to 8).find(!usedNumbers.contains(_)).getOrElse(2)

      // Insert the new layer before the last row (row 9)
      val newLayer = MaterialLayer.empty(nextNr)
      val updatedMaterials = (currentMaterials.filterNot(_.nr == 9) :+ newLayer :+ currentMaterials.find(_.nr == 9).get).sortBy(_.nr)

      calc.copy(
        sollCalculation = calc.sollCalculation.copy(materials = updatedMaterials)
      )
    )

  /** Remove a material layer from IST table */
  def removeIstMaterialLayer(id: String, layerNr: Int): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        istCalculation = calc.istCalculation.copy(
          materials = calc.istCalculation.materials.filterNot(m => m.nr == layerNr && m.isEditable)
        )
      )
    )

  /** Remove a material layer from SOLL table */
  def removeSollMaterialLayer(id: String, layerNr: Int): Unit =
    updateCalculation(id, calc =>
      calc.copy(
        sollCalculation = calc.sollCalculation.copy(
          materials = calc.sollCalculation.materials.filterNot(m => m.nr == layerNr && m.isEditable)
        )
      )
    )

  /** Get a specific calculation by ID */
  def getCalculation(id: String): Signal[Option[UWertCalculation]] =
    calculations.signal.map(_.find(_.id == id))

  /** Clear all calculations */
  def clear(): Unit =
    calculations.set(List.empty)

  /** Save calculations to project */
  def saveToProject(project: GeakProject): GeakProject =
    project.copy(uwertCalculations = calculations.now())

end UWertState

