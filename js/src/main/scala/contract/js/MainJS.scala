package contract.js

import contract.model.*
import contract.diagram.*
import contract.js.scenarios.*
import contract.js.interactive.*
import contract.js.mermaid.MermaidPage
import org.scalajs.dom
import scalatags.JsDom.all.*
import org.scalajs.dom.{HTMLElement, Node, document, html}

import scala.scalajs.js.Dynamic.global
import scala.concurrent.Future
import scala.concurrent.duration.given
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel
import upickle.default.*
import scala.util.control.NonFatal

lazy val mermaidPage = {
  val page = MermaidPage()

  EventBus.activeTestScenario.subscribe { scenario =>
    try {
      val request         = read[CreateContractRequest](scenario.input)
      val mermaidMarkdown = CreateDraftAsMermaid(request)
      page.update(scenario, mermaidMarkdown)
    } catch {
      case NonFatal(e) =>
        page.updateError(scenario, s"We couldn't parse the scenario as a DraftContract: $e")
    }
  }
  page.element
}

lazy val interactivePage = {
  val page = div().render

  EventBus.activeTestScenario.subscribe { scenario =>
    try {
      val request  = read[CreateContractRequest](scenario.input)
      val messages = SvgInterpreter.messages(request)
      // TODO - scale the config based on the div size
      val config    = ui.Config.default()
      val component = InteractiveComponent(SvgInterpreter.actors, messages, config)
      page.innerHTML = ""
      page.appendChild(component)
    } catch {
      case NonFatal(e) =>
        page.innerHTML = ""
        page.appendChild(div(s"We couldn't parse the scenario as a DraftContract: $e").render)
    }
  }
  page
}

@JSExportTopLevel("createScenarioBuilder")
def createScenarioBuilder(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.replace(ScenarioBuilder().content)

@JSExportTopLevel("createSequenceDiagram")
def createSequenceDiagram(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.replace(mermaidPage)

@JSExportTopLevel("createInteractivePage")
def createInteractivePage(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.replace(interactivePage)

@JSExportTopLevel("createDiffPage")
def createDiffPage(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.placeholder("Diff", state)

@JSExportTopLevel("onComponentCreated")
def onComponentCreated(id: String) = UIComponent.byFunction(id).foreach(EventBus.tabOpen.publish)

@JSExportTopLevel("onComponentDestroyed")
def onComponentDestroyed(id: String) =
  UIComponent.byFunction(id).foreach(EventBus.tabClosed.publish)

@main
def layout(): Unit = {
  new Drawer(HtmlUtils.$("drawer")).refresh()

  global.window.createScenarioBuilder = createScenarioBuilder
  global.window.createSequenceDiagram = createSequenceDiagram
  global.window.createInteractivePage = createInteractivePage
  global.window.createDiffPage = createDiffPage

  global.window.onComponentDestroyed = onComponentDestroyed
  global.window.onComponentCreated = onComponentCreated
}
