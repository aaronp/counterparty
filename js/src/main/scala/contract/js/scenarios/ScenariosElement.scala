package contract.js.scenarios
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.{MouseEvent, document, html, window}
import scalatags.JsDom.all.*
import scalatags.JsDom.tags2.section

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import scala.scalajs.js.annotation.*
import contract.js.LocalState
import contract.js.TestScenario
import contract.js.Material
import org.scalajs.dom.HTMLParagraphElement

case class ScenariosElement() {

  def render = {
    div(
      cls := "container",
      div(
        cls := "row",
        div(
          cls := "col s6", // Left side
          div(
            cls := "input-field",
            textarea(
              id    := "input-text-area",
              cls   := "materialize-textarea white-bg",
              style := "background-color: white",
              rows  := 40,
              label("Input Text")
            ),
            a(
              cls := "btn waves-effect waves-light",
              id  := "test-button",
              "Run" // Test button below text area
            )
          )
        ),
        div(
          cls := "col s6", // Right side
          div(
            cls := "input-field",
            textarea(
              id   := "results-text-area",
              cls  := "materialize-textarea",
              rows := 40,
              label("Results")
            )
          )
        )
      )
    ).render
  }

}
