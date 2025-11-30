package com.example.geak4s

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExportTopLevel

object Main:

  @JSExportTopLevel("main")
  def main(args: Array[String] = Array.empty): Unit =
    lazy val appContainer = dom.document.querySelector("#app")
    renderOnDomContentLoaded(appContainer, page)
  end main

  private lazy val page =
    val currentViewVar = Var[String]("hello")

    div(
      width := "100%",
      height := "100%",
      className := "app-container",
      Bar(
        _.design := BarDesign.Header,
        _.slots.startContent := div(
          className := "nav-buttons",
          Button(
            _.design <-- currentViewVar.signal.map(v => if v == "hello" then ButtonDesign.Emphasized else ButtonDesign.Transparent),
            _.events.onClick.mapTo("hello") --> currentViewVar,
            "Hello World"
          ),
          Button(
            _.design <-- currentViewVar.signal.map(v => if v == "excel" then ButtonDesign.Emphasized else ButtonDesign.Transparent),
            _.events.onClick.mapTo("excel") --> currentViewVar,
            "Excel Demo"
          )
        ),
        _.slots.endContent := Link(
          _.href := "https://github.com/taamepar/geak4s",
          _.target := LinkTarget._blank,
          "GitHub"
        ),
        Title(_.size := TitleLevel.H4, "Geak4s - Scala.js Application")
      ),
      div(
        className := "main-content",
        child <-- currentViewVar.signal.map {
          case "hello" => HelloWorldView()
          case "excel" => ExcelDemoView()
          case _ => HelloWorldView()
        }
      ),
      div(
        className := "footer",
        p("Built with Scala.js + Laminar + UI5 Web Components + SheetJS")
      )
    )
end Main

