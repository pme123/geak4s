package pme123.geak4s

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js.annotation.JSExportTopLevel
import pme123.geak4s.state.AppState
import pme123.geak4s.views.{WelcomeView, ProjectEditorView}
import pme123.geak4s.data.SwissZipCodes

object Main:

  @JSExportTopLevel("main")
  def main(args: Array[String] = Array.empty): Unit =
    // Load Swiss cities from CSV on startup
    SwissZipCodes.loadCities()

    lazy val appContainer = dom.document.querySelector("#app")
    renderOnDomContentLoaded(appContainer, page)
  end main

  private lazy val page =
    div(
      width := "100%",
      height := "100%",
      className := "app-container",

      // Main content - switches between Welcome and Project Editor
      child <-- AppState.currentView.signal.map {
        case AppState.View.Welcome => WelcomeView()
        case AppState.View.ProjectEditor => ProjectEditorView()
      }
    )
end Main

