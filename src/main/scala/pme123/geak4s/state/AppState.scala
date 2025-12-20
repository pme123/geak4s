package pme123.geak4s.state

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.concurrent.duration.*
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import pme123.geak4s.domain.*
import pme123.geak4s.domain.project.*
import pme123.geak4s.domain.building.*
import pme123.geak4s.domain.envelope.*
import pme123.geak4s.domain.hvac.*
import pme123.geak4s.domain.energy.*
import pme123.geak4s.services.GoogleDriveService

/** Application state management */
object AppState:
  
  /** Current view in the application */
  enum View:
    case Welcome
    case ProjectEditor
    case WorkflowEditor  // New workflow-based editor
  
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

  /** Google Drive sync state */
  val driveConnected: Var[Boolean] = Var(false)
  val driveSyncing: Var[Boolean] = Var(false)
  val driveLoginPrompt: Var[Boolean] = Var(false) // Shows when auto-login is triggered
  val driveError: Var[Option[String]] = Var(None) // Shows configuration or other errors
  val lastSyncTime: Var[Option[Long]] = Var(None)
  val autoSaveEnabled: Var[Boolean] = Var(true) // Each user logs in with their own account

  // Auto-save timer
  private var autoSaveTimer: Option[Int] = None
  private val AUTO_SAVE_DELAY_MS = 5000 // 5 seconds after last change
  
  /** Navigation helpers */
  def navigateToWelcome(): Unit = currentView.set(View.Welcome)
  def navigateToProjectEditor(): Unit = currentView.set(View.ProjectEditor)
  def navigateToWorkflowEditor(): Unit = currentView.set(View.WorkflowEditor)
  
  /** Project management */
  def createNewProject(): Unit =
    val emptyProject = GeakProject.empty
    projectState.set(ProjectState.Loaded(emptyProject, "geak_newproject.xlsx"))
    navigateToWorkflowEditor()  // Use workflow editor by default

  def createExampleProject(): Unit =
    val exampleProject = GeakProject.example
    projectState.set(ProjectState.Loaded(exampleProject, "geak_example.xlsx"))
    navigateToWorkflowEditor()  // Use workflow editor by default

  def loadProject(project: GeakProject, fileName: String): Unit =
    projectState.set(ProjectState.Loaded(project, fileName))
    // Initialize U-Wert state from project
    UWertState.loadFromProject(project)
    navigateToWorkflowEditor()  // Use workflow editor by default
  
  def setLoading(fileName: String): Unit =
    projectState.set(ProjectState.Loading(fileName))
  
  def setError(message: String): Unit =
    projectState.set(ProjectState.Error(message))
  
  def clearProject(): Unit =
    projectState.set(ProjectState.NoProject)
    UWertState.clear()
    navigateToWelcome()
  
  /** Get current project if loaded */
  def getCurrentProject: Option[GeakProject] =
    projectState.now() match
      case ProjectState.Loaded(project, _) => Some(project)
      case _ => None
  
  /** Update current project (with auto-save trigger) */
  def updateProject(updater: GeakProject => GeakProject): Unit =
    projectState.now() match
      case ProjectState.Loaded(project, fileName) =>
        projectState.set(ProjectState.Loaded(updater(project), fileName))
        triggerAutoSave()
      case _ => // ignore if no project loaded

  /** Save U-Wert calculations to current project */
  def saveUWertCalculations(): Unit =
    updateProject(project => UWertState.saveToProject(project))

  /** Initialize Google Drive integration */
  def initializeGoogleDrive(): Unit =
    GoogleDriveService.initialize()
    if GoogleDriveService.isSignedIn then
      driveConnected.set(true)

  /** Sign in to Google Drive */
  def signInToGoogleDrive(): Unit =
    GoogleDriveService.signIn().foreach { success =>
      driveConnected.set(success)
      if success then
        dom.console.log("Successfully connected to Google Drive")
        // Trigger initial save if project is loaded
        triggerAutoSave()
    }

  /** Sign out from Google Drive */
  def signOutFromGoogleDrive(): Unit =
    GoogleDriveService.signOut()
    driveConnected.set(false)
    lastSyncTime.set(None)

  /** Manually sync project to Google Drive (will prompt for login if needed) */
  def syncToGoogleDrive(): Unit =
    getCurrentProject match
      case Some(project) =>
        // Check if Google Drive is configured
        if !GoogleDriveService.isConfigured then
          driveError.set(Some("Google Drive ist nicht konfiguriert. Bitte aktualisieren Sie GoogleDriveConfig.scala mit Ihrer Client ID."))
          dom.console.warn("Google Drive is not configured. See documentation for setup instructions.")
          return

        driveSyncing.set(true)
        driveError.set(None)

        // Show login prompt if not connected
        if !GoogleDriveService.isSignedIn then
          driveLoginPrompt.set(true)

        val projectName = project.project.projectName match
          case name if name.nonEmpty => name
          case _ => "Unnamed_Project"

        // saveProjectState will automatically prompt for login if not signed in
        GoogleDriveService.saveProjectState(project, projectName).foreach { success =>
          driveSyncing.set(false)
          driveLoginPrompt.set(false)
          if success then
            // Update connection status if login was successful
            if !driveConnected.now() then
              driveConnected.set(true)
            lastSyncTime.set(Some(System.currentTimeMillis()))
            driveError.set(None)
            dom.console.log("Project synced to Google Drive")
          else
            if !GoogleDriveService.isConfigured then
              driveError.set(Some("Google Drive ist nicht konfiguriert"))
            else
              driveError.set(Some("Synchronisierung fehlgeschlagen"))
            dom.console.error("Failed to sync project to Google Drive")
        }
      case None =>
        dom.console.warn("No project loaded to sync")

  /** Trigger auto-save with debouncing */
  private def triggerAutoSave(): Unit =
    if !autoSaveEnabled.now() || !driveConnected.now() then
      return

    // Cancel existing timer
    autoSaveTimer.foreach(dom.window.clearTimeout)

    // Set new timer
    val timerId = dom.window.setTimeout(() => {
      syncToGoogleDrive()
    }, AUTO_SAVE_DELAY_MS)

    autoSaveTimer = Some(timerId)

  /** Toggle auto-save */
  def toggleAutoSave(): Unit =
    autoSaveEnabled.update(!_)
    if autoSaveEnabled.now() then
      triggerAutoSave()

end AppState

