package pme123.geak4s

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js.annotation.JSExportTopLevel
import pme123.geak4s.state.AppState
import pme123.geak4s.views.{WelcomeView, ProjectEditorView, WorkflowView}

object Main:

  @JSExportTopLevel("main")
  def main(args: Array[String] = Array.empty): Unit =
    // Initialize application

    lazy val appContainer = dom.document.querySelector("#app")
    renderOnDomContentLoaded(appContainer, page)
  end main

  private lazy val page =
    div(
      width := "100%",
      height := "100%",
      className := "app-container",

      // Main content - switches between Welcome, Project Editor, and Workflow Editor
      child <-- AppState.currentView.signal.map {
        case AppState.View.Welcome => WelcomeView()
        case AppState.View.ProjectEditor => ProjectEditorView()
        case AppState.View.WorkflowEditor => WorkflowView()
      }
    )
end Main

