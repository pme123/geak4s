package pme123.geak4s.state

import com.raquo.laminar.api.L.*

/** 
 * Workflow state management for the GEAK process
 * 
 * This manages the step-by-step workflow that guides users through the GEAK assessment process:
 * 1. Project Setup - Basic project information
 * 2. GIS Data - Fetch building data from cantonal GIS
 * 3. Calculations - Energy calculations and consumption data
 * 4. Inspection - On-site inspection protocol (tablet-friendly)
 * 5. Data Entry - Complete building envelope, HVAC, energy data
 * 6. Reports - Generate final GEAK report and export
 */
object WorkflowState:
  
  /** Workflow steps matching the GEAK process */
  enum Step(val order: Int, val title: String, val description: String):
    case ProjectSetup extends Step(1, "Projekt einrichten", "Projektinformationen und Ordnerstruktur")
    case GISData extends Step(2, "GIS-Daten", "Gebäudedaten vom kantonalen GIS beziehen")
    case Calculations extends Step(3, "Berechnungen", "Energiebezugsfläche und Verbrauchszahlen")
    case Inspection extends Step(4, "Begehung", "Begehungsprotokoll vor Ort ausfüllen")
    case DataEntry extends Step(5, "Dateneingabe", "Gebäudehülle, HLKK, Energie")
    case Reports extends Step(6, "Berichte", "GEAK-Bericht erstellen und exportieren")
  
  /** Status of each workflow step */
  enum StepStatus:
    case NotStarted
    case InProgress
    case Completed
    case Skipped
  
  /** Current workflow step */
  val currentStep: Var[Step] = Var(Step.ProjectSetup)
  
  /** Status of each step */
  val stepStatuses: Var[Map[Step, StepStatus]] = Var(
    Step.values.map(step => step -> StepStatus.NotStarted).toMap
  )
  
  /** Navigation helpers */
  def goToStep(step: Step): Unit = 
    currentStep.set(step)
  
  def nextStep(): Unit =
    val current = currentStep.now()
    val nextStepOpt = Step.values.find(_.order == current.order + 1)
    nextStepOpt.foreach(goToStep)
  
  def previousStep(): Unit =
    val current = currentStep.now()
    val prevStepOpt = Step.values.find(_.order == current.order - 1)
    prevStepOpt.foreach(goToStep)
  
  def canGoNext: Signal[Boolean] = 
    currentStep.signal.map(step => step.order < Step.values.length)
  
  def canGoPrevious: Signal[Boolean] = 
    currentStep.signal.map(step => step.order > 1)
  
  /** Mark step as completed */
  def completeStep(step: Step): Unit =
    stepStatuses.update(statuses => statuses + (step -> StepStatus.Completed))
  
  def markInProgress(step: Step): Unit =
    stepStatuses.update(statuses => statuses + (step -> StepStatus.InProgress))
  
  /** Get status of a step */
  def getStepStatus(step: Step): Signal[StepStatus] =
    stepStatuses.signal.map(_.getOrElse(step, StepStatus.NotStarted))
  
  /** Reset workflow */
  def reset(): Unit =
    currentStep.set(Step.ProjectSetup)
    stepStatuses.set(Step.values.map(step => step -> StepStatus.NotStarted).toMap)
  
  /** Calculate overall progress percentage */
  def progressPercentage: Signal[Int] =
    stepStatuses.signal.map { statuses =>
      val completed = statuses.values.count(_ == StepStatus.Completed)
      val total = Step.values.length
      (completed * 100 / total)
    }

end WorkflowState

