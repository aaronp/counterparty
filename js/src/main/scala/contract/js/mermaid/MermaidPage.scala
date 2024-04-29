package contract.js.mermaid

import contract.DraftContract
import org.scalajs.dom
import org.scalajs.dom.{Node, document, html}
import scalatags.JsDom.all.*
import ujson.Value

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSGlobal("renderMermaid")
object RenderMermaid extends js.Object {
  def apply(targetElm: Node): Unit = js.native
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
