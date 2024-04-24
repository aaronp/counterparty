package contract.js.mermaid

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

import contract.DraftContract
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.{MouseEvent, document, html, window}
import scalatags.JsDom.all.*
import scalatags.JsDom.tags2.section
import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom
import org.scalajs.dom.Node
import org.scalajs.dom.document
import scalatags.JsDom.all._

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import scala.scalajs.js.annotation.*
import contract.js.LocalState
import contract.js.TestScenario
import contract.js.Material
import org.scalajs.dom.HTMLParagraphElement
import upickle.default.*
import ujson.Value

@js.native
@JSGlobal("renderMermaid")
object RenderMermaid extends js.Object {
  def apply(targetElm: Node): Unit = js.native
}

trait MermaidAPI extends js.Object {
  def render(id: String, graph: String): js.Promise[String] // = js.native
}

@JSImport("mermaid", JSImport.Namespace)
@js.native
object Mermaid extends js.Object {
  def mermaidAPI: MermaidAPI = js.native
  // def renderMermaid(id: String, graph: String, callback: js.Function1[js.Any, Unit]): Unit =
  //   js.native
}

case class MermaidPage(initialGraphData: String = "graph TD; A-->B; A-->C; B-->D; C-->D;") {

  private def defaultJason: Value = ujson.read(DraftContract.testData.asData.asJson)

  val graphDefinition = """
  graph TD;
  A-->B;
  A-->C;
  B-->D;
  C-->D;
"""

  private def renderDiagram(containerId: String, graphDefinition: String): Unit = {
    println(s"$containerId, $graphDefinition")
    val renderFuture = Mermaid.mermaidAPI.render(containerId, graphDefinition)

    renderFuture.`then`(svgGraph => {
      val container = document.getElementById(containerId)
      container.innerHTML = svgGraph.toString
    })
  }

  // Create the text area for Mermaid markup
  private val textArea = textarea(
    id   := "mermaidInput",
    rows := 6,
    cols := 50,
    initialGraphData
  ).render

  // Create a button to render the diagram
  private val renderButton = button(
    "Render Diagram",
    onclick := (() => renderDiagram())
  ).render

  // Create a div to display the diagram
  private val diagramDiv = div(
    id        := "diagram",
    border    := "1px solid #ccc",
    padding   := "20px",
    marginTop := "20px"
  ).render

  // Append the elements to the body
  def element = {
    div(
      h1("Mermaid Diagram Renderer"),
      p("Enter Mermaid markup below to render a diagram."),
      textArea,
      renderButton,
      diagramDiv
    ).render
  }

  def renderDiagram(): Unit = {
    val input = dom.document.getElementById("mermaidInput").asInstanceOf[dom.html.TextArea].value
    diagramDiv.innerHTML = ""

    val newTargetNode = dom.document.createElement("div")
    newTargetNode.setAttribute("id", "diagram")
    newTargetNode.textContent = textArea.value
    diagramDiv.appendChild(newTargetNode)

    RenderMermaid(newTargetNode)

  }
}
