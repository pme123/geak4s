package com.example.geak4s

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}

object HelloWorldView:

  def apply(): HtmlElement =
    div(
      className := "hello-world-container",
      renderExample()
    )

  def renderExample(): HtmlElement =
    val nameVar = Var(initial = "World")
    
    div(
      Card(
        _.slots.header := CardHeader(
          _.titleText := "Hello World Demo",
          _.subtitleText := "Interactive Laminar Example"
        ),
        div(
          className := "card-content",
          Label(
            _.forId := "name-input",
            "Your name:"
          ),
          Input(
            _.id := "name-input",
            _.placeholder := "Enter your name here",
            _.events.onInput.mapToValue --> nameVar
          ),
          div(
            className := "greeting",
            Title(
              _.size := TitleLevel.H3,
              "Hello, ",
              text <-- nameVar.signal.map(_.toUpperCase),
              "! 👋"
            )
          )
        )
      )
    )
end HelloWorldView

