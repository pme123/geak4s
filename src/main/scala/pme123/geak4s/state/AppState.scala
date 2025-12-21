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
  val syncInitialized: Var[Boolean] = Var(false) // Tracks if sync has been explicitly started for this project

  // Auto-save timer
  private var autoSaveTimer: Option[Int] = None
  private val AUTO_SAVE_DELAY_MS = 5000 // 5 seconds after last change

  // Periodic sync timer
  private var periodicSyncTimer: Option[Int] = None
  private val PERIODIC_SYNC_INTERVAL_MS = 30000 // 30 seconds
  
  /** Navigation helpers */
  def navigateToWelcome(): Unit = currentView.set(View.Welcome)
  def navigateToProjectEditor(): Unit = currentView.set(View.ProjectEditor)
  def navigateToWorkflowEditor(): Unit = currentView.set(View.WorkflowEditor)
  
  /** Project management */
  def createNewProject(): Unit =
    val emptyProject = GeakProject.empty
    projectState.set(ProjectState.Loaded(emptyProject, "geak_newproject.xlsx"))
    navigateToWorkflowEditor()  // Use workflow editor by default
    // Don't auto-connect for new projects - wait until project name is set

  def createExampleProject(): Unit =
    val exampleProject = GeakProject.example
    projectState.set(ProjectState.Loaded(exampleProject, "geak_example.xlsx"))
    navigateToWorkflowEditor()  // Use workflow editor by default
    // Auto-connect for example projects since they already have a name
    autoConnectToGoogleDrive()

  def loadProject(project: GeakProject, fileName: String): Unit =
    projectState.set(ProjectState.Loaded(project, fileName))
    // Initialize U-Wert state from project
    UWertState.loadFromProject(project)
    // Initialize Area state from project
    AreaState.loadFromProject(project)
    // Mark sync as initialized for loaded projects (existing projects should auto-sync)
    syncInitialized.set(true)
    // Auto-connect to Google Drive for loaded projects
    autoConnectToGoogleDrive()
    navigateToWorkflowEditor()  // Use workflow editor by default
  
  def setLoading(fileName: String): Unit =
    projectState.set(ProjectState.Loading(fileName))
  
  def setError(message: String): Unit =
    projectState.set(ProjectState.Error(message))
  
  def clearProject(): Unit =
    projectState.set(ProjectState.NoProject)
    UWertState.clear()
    AreaState.clear()
    stopPeriodicSync()
    syncInitialized.set(false)
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
        println(s"Updating project: ${project.project.projectName} - $fileName")
        projectState.set(ProjectState.Loaded(updater(project), fileName))
        triggerAutoSave()
      case _ => // ignore if no project loaded

  /** Save U-Wert calculations to current project */
  def saveUWertCalculations(): Unit =
    updateProject(project => UWertState.saveToProject(project))

  /** Save area calculations to current project */
  def saveAreaCalculations(): Unit =
    updateProject(project => AreaState.saveToProject(project))

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
        // Mark sync as initialized when manually connecting
        if getCurrentProject.isDefined && !syncInitialized.now() then
          syncInitialized.set(true)
        // Trigger initial save if project is loaded
        triggerAutoSave()
        // Start periodic sync
        startPeriodicSync()
    }

  /** Auto-connect to Google Drive when creating or loading a project */
  private def autoConnectToGoogleDrive(): Unit =
    // Check if Google Drive is configured
    if !GoogleDriveService.isConfigured then
      dom.console.warn("Google Drive is not configured - skipping auto-connect")
      return

    // If already connected, just initialize sync
    if driveConnected.now() then
      dom.console.log("Already connected to Google Drive - initializing sync")
      if !syncInitialized.now() then
        syncInitialized.set(true)
      startPeriodicSync()
      return

    // Auto-connect to Google Drive
    dom.console.log("Auto-connecting to Google Drive...")
    GoogleDriveService.signIn().foreach { success =>
      driveConnected.set(success)
      if success then
        dom.console.log("Successfully auto-connected to Google Drive")
        // Mark sync as initialized for auto-connected projects
        syncInitialized.set(true)
        // Trigger initial save
        triggerAutoSave()
        // Start periodic sync
        startPeriodicSync()
      else
        dom.console.warn("Failed to auto-connect to Google Drive")
    }

  /** Sign out from Google Drive */
  def signOutFromGoogleDrive(): Unit =
    GoogleDriveService.signOut()
    driveConnected.set(false)
    lastSyncTime.set(None)
    stopPeriodicSync()

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
    // Only auto-save if sync has been initialized
    if !syncInitialized.now() || !autoSaveEnabled.now() || !driveConnected.now() then
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

  /** Initialize sync for a new project - connects to Google Drive and starts auto-save and periodic sync */
  def initializeSync(): Unit =
    // Check if project name is set
    getCurrentProject match
      case Some(project) if project.project.projectName.trim.isEmpty =>
        driveError.set(Some("Bitte geben Sie zuerst eine Projektbezeichnung ein."))
        dom.console.warn("Cannot initialize sync: project name is empty")
        return
      case None =>
        driveError.set(Some("Kein Projekt geladen."))
        dom.console.warn("Cannot initialize sync: no project loaded")
        return
      case _ => // Project name is set, continue

    dom.console.log("Initializing sync for project - connecting to Google Drive")
    syncInitialized.set(true)
    // Connect to Google Drive
    autoConnectToGoogleDrive()

  /** Start periodic sync (every 30 seconds) */
  private def startPeriodicSync(): Unit =
    // Only start if sync is initialized, connected and project is loaded
    if !syncInitialized.now() || !driveConnected.now() || getCurrentProject.isEmpty then
      return

    // Stop any existing timer
    stopPeriodicSync()

    // Set up periodic sync
    val timerId = dom.window.setInterval(() => {
      if driveConnected.now() && getCurrentProject.isDefined then
        syncToGoogleDrive()
    }, PERIODIC_SYNC_INTERVAL_MS)

    periodicSyncTimer = Some(timerId)
    dom.console.log("Started periodic sync (every 30 seconds)")

  /** Stop periodic sync */
  private def stopPeriodicSync(): Unit =
    periodicSyncTimer.foreach { timerId =>
      dom.window.clearInterval(timerId)
      dom.console.log("Stopped periodic sync")
    }
    periodicSyncTimer = None

end AppState

