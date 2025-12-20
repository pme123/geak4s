package pme123.geak4s.config

import scala.scalajs.js
import org.scalajs.dom

/**
 * Configuration for Google Drive integration
 *
 * Credentials are loaded from environment variables:
 * - GEAK4S_GOOGLE_CLIENTID: OAuth 2.0 Client ID
 * - GEAK4S_GOOGLE_CLIENTSECRET: Client Secret (used as API Key)
 *
 * To set up locally:
 * 1. Set environment variables before running sbt:
 *    export GEAK4S_GOOGLE_CLIENTID=your-client-id
 *    export GEAK4S_GOOGLE_CLIENTSECRET=your-client-secret
 * 2. Or use a .env file with direnv or similar tool
 *
 * For GitHub Pages:
 * 1. Go to repository Settings > Secrets and variables > Actions
 * 2. Add secrets: GEAK4S_GOOGLE_CLIENTID and GEAK4S_GOOGLE_CLIENTSECRET
 * 3. Update .github/workflows/deploy.yml to use these secrets
 */
object GoogleDriveConfig:

  /**
   * OAuth 2.0 Client ID from Google Cloud Console
   * Loaded from environment variable GEAK4S_GOOGLE_CLIENTID (via JavaScript global)
   */
  val clientId: String =
    val envClientId = js.Dynamic.global.GEAK4S_GOOGLE_CLIENTID
    if js.isUndefined(envClientId) || envClientId == null || envClientId.toString.isEmpty then
      dom.console.warn("⚠️ GEAK4S_GOOGLE_CLIENTID not set. Please configure environment variables.")
      ""
    else
      envClientId.toString

  /**
   * Client Secret from Google Cloud Console (used as API Key)
   * Loaded from environment variable GEAK4S_GOOGLE_CLIENTSECRET (via JavaScript global)
   */
  val apiKey: String =
    val envApiKey = js.Dynamic.global.GEAK4S_GOOGLE_CLIENTSECRET
    if js.isUndefined(envApiKey) || envApiKey == null || envApiKey.toString.isEmpty then
      dom.console.warn("⚠️ GEAK4S_GOOGLE_CLIENTSECRET not set. Client secret is optional but recommended.")
      ""
    else
      envApiKey.toString

  /**
   * Root folder name for GEAK projects in Google Drive
   * Based on your folder structure
   */
  val rootFolder: String = "GEAK4S"

  /**
   * Discovery docs for Google Drive API
   */
  val discoveryDocs: js.Array[String] = js.Array(
    "https://www.googleapis.com/discovery/v1/apis/drive/v3/rest"
  )

  /**
   * OAuth scopes required for Google Drive access
   * - drive.file: Access files created by this app
   * - drive: Full access to Google Drive (needed to create folders and upload files)
   */
  val scopes: String = "https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive"

  /**
   * Check if Google Drive is configured
   */
  def isConfigured: Boolean =
    !clientId.isEmpty && clientId.contains(".apps.googleusercontent.com")

end GoogleDriveConfig

