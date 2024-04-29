package contract.js

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

@JSExportTopLevel("createScenarioBuilder")
def createScenarioBuilder(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.replace(ScenarioBuilder().content)

@JSExportTopLevel("createSequenceDiagram")
def createSequenceDiagram(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.placeholder("Sequence Diagram", state)

@JSExportTopLevel("createInteractivePage")
def createInteractivePage(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.replace(
    TestData.interactive
  ) // TODO <- make this responsive design, and react to test scenarios

@JSExportTopLevel("createDiffPage")
def createDiffPage(container: scala.scalajs.js.Dynamic, state: scala.scalajs.js.Dynamic) =
  container.placeholder("Diff", state)

@JSExportTopLevel("onComponentCreated")
def onComponentCreated(id: String) = Components.byFunction(id).foreach(EventBus.tabOpen.publish)

@JSExportTopLevel("onComponentDestroyed")
def onComponentDestroyed(id: String) = Components.byFunction(id).foreach(EventBus.tabClosed.publish)

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
