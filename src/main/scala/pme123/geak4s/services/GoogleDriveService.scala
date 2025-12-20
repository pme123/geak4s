package pme123.geak4s.services

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.timers
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom
import pme123.geak4s.config.GoogleDriveConfig
import pme123.geak4s.domain.GeakProject
import pme123.geak4s.domain.JsonCodecs.given
import io.circe.syntax.*
import io.circe.parser.*

/**
 * Service for Google Drive integration using Google Drive API v3
 *
 * Features:
 * - Authentication via Google OAuth 2.0
 * - Create folders in Google Drive
 * - Upload files (Excel, JSON state)
 * - Download files
 * - Persist project state automatically
 */
object GoogleDriveService:

  private var gapiInited = false
  private var gisInited = false
  private var tokenClient: js.Dynamic = null
  private var accessToken: String = ""

  /**
   * Initialize Google Drive API
   * Call this once when the app starts
   */
  def initialize(): Unit =
    dom.console.log("=== Google Drive initialize() called ===")
    dom.console.log(s"Google Drive configured: ${GoogleDriveConfig.isConfigured}")
    dom.console.log(s"Client ID: ${GoogleDriveConfig.clientId}")

    if !GoogleDriveConfig.isConfigured then
      dom.console.warn("⚠️ Google Drive is not configured. Please check .env file")
      return

    try
      // Wait for Google API libraries to load
      waitForGoogleAPIs()
    catch
      case ex: Exception =>
        dom.console.error(s"❌ Failed to initialize Google Drive service: ${ex.getMessage}")

  /**
   * Wait for Google API libraries to load, then initialize
   */
  private def waitForGoogleAPIs(): Unit =
    val checkInterval = 100 // ms
    var attempts = 0
    val maxAttempts = 50 // 5 seconds total

    var intervalId: js.timers.SetIntervalHandle = null
    intervalId = js.timers.setInterval(checkInterval) {
      attempts += 1

      val gapi = js.Dynamic.global.gapi
      val google = js.Dynamic.global.google

      if !js.isUndefined(gapi) && !js.isUndefined(google) then
        js.timers.clearInterval(intervalId)
        dom.console.log("✅ Google API libraries loaded")
        initializeGapi()
        initializeGis()
      else if attempts >= maxAttempts then
        js.timers.clearInterval(intervalId)
        dom.console.error("❌ Timeout waiting for Google API libraries to load")
    }

  /**
   * Initialize Google API client (gapi)
   */
  private def initializeGapi(): Unit =
    val gapi = js.Dynamic.global.gapi

    dom.console.log("🔧 Initializing Google API client...")

    gapi.load("client", { () =>
      // Note: We don't use apiKey with the new GIS approach
      // The access token from GIS is used instead
      gapi.client.init(js.Dynamic.literal(
        discoveryDocs = js.Array("https://www.googleapis.com/discovery/v1/apis/drive/v3/rest")
      )).`then`({ () =>
        dom.console.log("✅ Google API client initialized")
        gapiInited = true
      }: js.Function0[Unit], { (error: js.Dynamic) =>
        dom.console.error("❌ Failed to initialize Google API client:")
        dom.console.error(error)
      }: js.Function1[js.Dynamic, Unit])
    }: js.Function0[Unit])

  /**
   * Initialize Google Identity Services (GIS)
   */
  private def initializeGis(): Unit =
    val google = js.Dynamic.global.google

    dom.console.log("🔧 Initializing Google Identity Services...")
    dom.console.log(s"Client ID: ${GoogleDriveConfig.clientId}")
    dom.console.log(s"Scopes: ${GoogleDriveConfig.scopes}")

    tokenClient = google.accounts.oauth2.initTokenClient(js.Dynamic.literal(
      client_id = GoogleDriveConfig.clientId,
      scope = GoogleDriveConfig.scopes,
      callback = { (response: js.Dynamic) =>
        if !js.isUndefined(response.error) then
          dom.console.error(s"❌ Token error: ${response.error}")
        else
          accessToken = response.access_token.toString
          dom.console.log("✅ Access token received")
      }: js.Function1[js.Dynamic, Unit]
    ))

    gisInited = true
    dom.console.log("✅ Google Identity Services initialized")

  /**
   * Sign in to Google Drive
   */
  def signIn(): Future[Boolean] =
    dom.console.log("=== Google Drive signIn() called ===")
    dom.console.log(s"isConfigured: ${GoogleDriveConfig.isConfigured}")
    dom.console.log(s"gapiInited: $gapiInited, gisInited: $gisInited")

    if !GoogleDriveConfig.isConfigured then
      dom.console.error("Cannot sign in - Google Drive is not configured!")
      dom.console.error("Please check your .env file")
      return Future.successful(false)

    if !gapiInited || !gisInited then
      dom.console.error("Cannot sign in - Google Drive not initialized!")
      return Future.successful(false)

    try
      dom.console.log("Opening Google sign-in popup...")

      val promise = Promise[Boolean]()

      // Set callback for this specific sign-in request
      tokenClient.callback = { (response: js.Dynamic) =>
        if !js.isUndefined(response.error) then
          dom.console.error(s"❌ Sign in failed: ${response.error}")
          promise.success(false)
        else
          accessToken = response.access_token.toString
          dom.console.log("✅ Signed in to Google Drive")

          // Set the access token for gapi client
          val gapi = js.Dynamic.global.gapi
          gapi.client.setToken(js.Dynamic.literal(access_token = accessToken))

          promise.success(true)
      }: js.Function1[js.Dynamic, Unit]

      // Request access token
      val gapi = js.Dynamic.global.gapi
      if js.isUndefined(gapi.client.getToken()) || gapi.client.getToken() == null then
        // Prompt for consent
        tokenClient.requestAccessToken(js.Dynamic.literal(prompt = "consent"))
      else
        // Skip consent if already granted
        tokenClient.requestAccessToken(js.Dynamic.literal(prompt = ""))

      promise.future
    catch
      case ex: Exception =>
        dom.console.error(s"❌ Sign in error: ${ex.getMessage}")
        Future.successful(false)

  /**
   * Sign out from Google Drive
   */
  def signOut(): Unit =
    val gapi = js.Dynamic.global.gapi
    val token = gapi.client.getToken()

    if !js.isUndefined(token) && token != null then
      val google = js.Dynamic.global.google
      google.accounts.oauth2.revoke(token.access_token)
      gapi.client.setToken(null)
      accessToken = ""
      dom.console.log("✅ Signed out from Google Drive")

  /**
   * Check if user is signed in
   */
  def isSignedIn: Boolean =
    val gapi = js.Dynamic.global.gapi
    if js.isUndefined(gapi) || js.isUndefined(gapi.client) then
      false
    else
      val token = gapi.client.getToken()
      !js.isUndefined(token) && token != null && accessToken.nonEmpty

  /**
   * Check if Google Drive is configured
   */
  def isConfigured: Boolean = GoogleDriveConfig.isConfigured

  /**
   * Get current user's display name or email
   * Note: With the new GIS API, we don't have direct access to user profile
   * You would need to use the People API or userinfo endpoint for this
   */
  def getCurrentUserName: Option[String] =
    if !isSignedIn then None
    else Some("Google Drive User") // Simplified for now

  /**
   * Find or create a folder by path
   * @param folderPath Path like "folder1/folder2/folder3"
   * @return Future with folder ID
   */
  def findOrCreateFolder(folderPath: String): Future[Option[String]] =
    if folderPath.isEmpty then
      Future.successful(Some("root"))
    else
      val segments = folderPath.split("/").filter(_.nonEmpty)
      findOrCreateFolderRecursive(segments.toList, "root")

  /**
   * Recursively find or create folders
   */
  private def findOrCreateFolderRecursive(segments: List[String], parentId: String): Future[Option[String]] =
    segments match
      case Nil => Future.successful(Some(parentId))
      case folderName :: rest =>
        findFolder(folderName, parentId).flatMap {
          case Some(folderId) =>
            // Folder exists, continue with next segment
            findOrCreateFolderRecursive(rest, folderId)
          case None =>
            // Folder doesn't exist, create it
            createFolder(folderName, parentId).flatMap {
              case Some(folderId) =>
                findOrCreateFolderRecursive(rest, folderId)
              case None =>
                Future.successful(None)
            }
        }

  /**
   * Find a folder by name in a parent folder
   */
  private def findFolder(folderName: String, parentId: String): Future[Option[String]] =
    val promise = Promise[Option[String]]()

    try
      val gapi = js.Dynamic.global.gapi
      val query = s"name='$folderName' and '$parentId' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false"

      gapi.client.drive.files.list(js.Dynamic.literal(
        q = query,
        fields = "files(id, name)",
        spaces = "drive"
      )).`then`({ (response: js.Dynamic) =>
        val files = response.result.files.asInstanceOf[js.Array[js.Dynamic]]
        if files.length > 0 then
          promise.success(Some(files(0).id.toString))
        else
          promise.success(None)
      }: js.Function1[js.Dynamic, Unit], { (error: js.Dynamic) =>
        dom.console.error(s"Error finding folder: ${error.result.error.message}")
        promise.success(None)
      }: js.Function1[js.Dynamic, Unit])
    catch
      case ex: Exception =>
        dom.console.error(s"Error finding folder: ${ex.getMessage}")
        promise.success(None)

    promise.future

  /**
   * Create a folder in Google Drive
   */
  private def createFolder(folderName: String, parentId: String): Future[Option[String]] =
    val promise = Promise[Option[String]]()

    try
      val gapi = js.Dynamic.global.gapi

      val fileMetadata = js.Dynamic.literal(
        name = folderName,
        mimeType = "application/vnd.google-apps.folder",
        parents = js.Array(parentId)
      )

      gapi.client.drive.files.create(js.Dynamic.literal(
        resource = fileMetadata,
        fields = "id"
      )).`then`({ (response: js.Dynamic) =>
        val folderId = response.result.id.toString
        dom.console.log(s"✅ Created folder: $folderName (ID: $folderId)")
        promise.success(Some(folderId))
      }: js.Function1[js.Dynamic, Unit], { (error: js.Dynamic) =>
        dom.console.error(s"Error creating folder: ${error.result.error.message}")
        promise.success(None)
      }: js.Function1[js.Dynamic, Unit])
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating folder: ${ex.getMessage}")
        promise.success(None)

    promise.future

  /**
   * Upload a file to Google Drive
   * @param folderPath Folder path (e.g., "GEAK_Projects/Project1")
   * @param fileName File name
   * @param content File content as ArrayBuffer
   * @param mimeType MIME type of the file
   * @return Future with upload result
   */
  def uploadFile(folderPath: String, fileName: String, content: js.typedarray.ArrayBuffer, mimeType: String): Future[Boolean] =
    findOrCreateFolder(folderPath).flatMap {
      case Some(folderId) =>
        uploadFileToFolder(fileName, content, mimeType, folderId)
      case None =>
        dom.console.error(s"Failed to find or create folder: $folderPath")
        Future.successful(false)
    }

  /**
   * Upload file to a specific folder
   */
  private def uploadFileToFolder(fileName: String, content: js.typedarray.ArrayBuffer, mimeType: String, folderId: String): Future[Boolean] =
    val promise = Promise[Boolean]()

    try
      val gapi = js.Dynamic.global.gapi

      // Convert ArrayBuffer to base64
      val uint8Array = new js.typedarray.Uint8Array(content)
      val binary = uint8Array.foldLeft("")((acc, byte) => acc + byte.toChar)
      val base64 = dom.window.btoa(binary)

      val fileMetadata = js.Dynamic.literal(
        name = fileName,
        parents = js.Array(folderId)
      )

      val multipartBody =
        s"""--boundary
           |Content-Type: application/json; charset=UTF-8
           |
           |${js.JSON.stringify(fileMetadata)}
           |--boundary
           |Content-Type: $mimeType
           |Content-Transfer-Encoding: base64
           |
           |$base64
           |--boundary--""".stripMargin

      val request = gapi.client.request(js.Dynamic.literal(
        path = "/upload/drive/v3/files",
        method = "POST",
        params = js.Dynamic.literal(uploadType = "multipart"),
        headers = js.Dynamic.literal(
          `Content-Type` = "multipart/related; boundary=boundary"
        ),
        body = multipartBody
      ))

      request.`then`({ (response: js.Dynamic) =>
        dom.console.log(s"✅ Uploaded file: $fileName")
        promise.success(true)
      }: js.Function1[js.Dynamic, Unit], { (error: js.Dynamic) =>
        dom.console.error(s"Error uploading file: ${error.result.error.message}")
        promise.success(false)
      }: js.Function1[js.Dynamic, Unit])
    catch
      case ex: Exception =>
        dom.console.error(s"Error uploading file: ${ex.getMessage}")
        promise.success(false)

    promise.future

  /**
   * Save project state to Google Drive
   * Automatically prompts for login if not signed in
   */
  def saveProjectState(project: GeakProject, projectName: String): Future[Boolean] =
    dom.console.log("=== saveProjectState() called ===")
    dom.console.log(s"Project name: $projectName")
    dom.console.log(s"isConfigured: $isConfigured")
    dom.console.log(s"isSignedIn: $isSignedIn")

    if !isConfigured then
      dom.console.warn("⚠️ Google Drive is not configured...")
      Future.successful(false)
    else if !isSignedIn then
      dom.console.log("🔐 Not signed in to Google Drive - prompting for login")
      signIn().flatMap { success =>
        if success then saveProjectStateInternal(project, projectName)
        else Future.successful(false)
      }
    else
      saveProjectStateInternal(project, projectName)

  /**
   * Internal method to save project state (assumes already signed in)
   */
  private def saveProjectStateInternal(project: GeakProject, projectName: String): Future[Boolean] =
    try
      val sanitizedName = projectName.replaceAll("[^a-zA-Z0-9-_]", "_")
      val projectFolder = s"${GoogleDriveConfig.rootFolder}/$sanitizedName"
      val fileName = "project_state.json"

      // Convert project to JSON
      val jsonString = project.asJson.noSpaces
      val jsonBytes = jsonString.getBytes("UTF-8")
      val arrayBuffer = js.typedarray.ArrayBuffer(jsonBytes.length)
      val uint8Array = new js.typedarray.Uint8Array(arrayBuffer)
      jsonBytes.zipWithIndex.foreach { case (byte, i) => uint8Array(i) = byte }

      uploadFile(projectFolder, fileName, arrayBuffer, "application/json")
    catch
      case ex: Exception =>
        dom.console.error(s"Failed to save project state: ${ex.getMessage}")
        Future.successful(false)

  /**
   * Upload Excel file to Google Drive
   * Automatically prompts for login if not signed in
   */
  def uploadExcelFile(project: GeakProject, projectName: String, excelBuffer: js.typedarray.ArrayBuffer): Future[Boolean] =
    if !isSignedIn then
      dom.console.log("Not signed in to Google Drive - prompting for login")
      signIn().flatMap { success =>
        if success then uploadExcelFileInternal(project, projectName, excelBuffer)
        else Future.successful(false)
      }
    else
      uploadExcelFileInternal(project, projectName, excelBuffer)

  /**
   * Internal method to upload Excel file (assumes already signed in)
   */
  private def uploadExcelFileInternal(project: GeakProject, projectName: String, excelBuffer: js.typedarray.ArrayBuffer): Future[Boolean] =
    try
      val sanitizedName = projectName.replaceAll("[^a-zA-Z0-9-_]", "_")
      val projectFolder = s"${GoogleDriveConfig.rootFolder}/$sanitizedName"
      val fileName = s"${sanitizedName}_${System.currentTimeMillis()}.xlsx"

      uploadFile(projectFolder, fileName, excelBuffer, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    catch
      case ex: Exception =>
        dom.console.error(s"Failed to upload Excel file: ${ex.getMessage}")
        Future.successful(false)

end GoogleDriveService



