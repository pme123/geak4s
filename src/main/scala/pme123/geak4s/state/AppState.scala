package pme123.geak4s.state

import com.raquo.laminar.api.L.*
import pme123.geak4s.domain.*
import pme123.geak4s.domain.project.*
import pme123.geak4s.domain.building.*
import pme123.geak4s.domain.envelope.*
import pme123.geak4s.domain.hvac.*
import pme123.geak4s.domain.energy.*

/** Application state management */
object AppState:
  
  /** Current view in the application */
  enum View:
    case Welcome
    case ProjectEditor
  
  /** Current project state */
  sealed trait ProjectState
  object ProjectState:
    case object NoProject extends ProjectState
    case class Loading(fileName: String) extends ProjectState
    case class Loaded(project: GeakProject, fileName: String) extends ProjectState
    case class Error(message: String) extends ProjectState
  
  /** Global application state */
  val currentView: Var[View] = Var(View.Welcome)
  val projectState: Var[ProjectState] = Var(ProjectState.NoProject)

  /** Signal for current project */
  val projectSignal: Signal[Option[GeakProject]] = projectState.signal.map {
    case ProjectState.Loaded(project, _) => Some(project)
    case _ => None
  }
  
  /** Navigation helpers */
  def navigateToWelcome(): Unit = currentView.set(View.Welcome)
  def navigateToProjectEditor(): Unit = currentView.set(View.ProjectEditor)
  
  /** Project management */
  def createNewProject(): Unit =
    val emptyProject = GeakProject.empty
    projectState.set(ProjectState.Loaded(emptyProject, "geak_newproject.xlsx"))
    navigateToProjectEditor()
  
  def loadProject(project: GeakProject, fileName: String): Unit =
    projectState.set(ProjectState.Loaded(project, fileName))
    navigateToProjectEditor()
  
  def setLoading(fileName: String): Unit =
    projectState.set(ProjectState.Loading(fileName))
  
  def setError(message: String): Unit =
    projectState.set(ProjectState.Error(message))
  
  def clearProject(): Unit =
    projectState.set(ProjectState.NoProject)
    navigateToWelcome()
  
  /** Get current project if loaded */
  def getCurrentProject: Option[GeakProject] =
    projectState.now() match
      case ProjectState.Loaded(project, _) => Some(project)
      case _ => None
  
  /** Update current project */
  def updateProject(updater: GeakProject => GeakProject): Unit =
    projectState.now() match
      case ProjectState.Loaded(project, fileName) =>
        projectState.set(ProjectState.Loaded(updater(project), fileName))
      case _ => // ignore if no project loaded

end AppState

