package contract.js

import contract.js.scenarios.*
import contract.js.interactive.*
import contract.js.mermaid.MermaidPage
import org.scalajs.dom
import scalatags.JsDom.all.*
import org.scalajs.dom.{HTMLElement, Node, document, html}

import scala.scalajs.js.Dynamic.{global => g, literal => lit}
import scala.concurrent.Future
import scala.concurrent.duration.given
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel

/** TODO:
  * {{{
  * $ Mermaid which is driven by test scenarios, and auto-resizes: dynamic resizing (using ResizeObserver ?)
  * $ Interactive which is driven by test scenarios, and auto-resizes: dynamic resizing (using ResizeObserver ?)
  * $ errors go to Footer
  * }}}
  * @param parent
  *   the parent element
  */
case class Drawer(parent: HTMLElement) {

  /** The idea is that we can remove elements based on the current state of which apps have been
    * added
    *
    * @param inactiveTabs
    *   this is our filter -- only show the tabs which currently aren't on the screen
    * @return
    *   nowt - this updates the components in-place
    */
  def refresh(inactiveTabs: Set[Components] = Components.values.toSet) = {
    parent.innerHTML = ""

    def add(title: String, function: String) = {

      val config = JSON.parse(s"""{
          "type": "component",
          "componentName": "$function",
          "title": "$title",
          "componentState": {
              "text": "text"
          }}""")

      val item = li(cls := "draggable", title).render
      parent.appendChild(item)
      myLayout.createDragSource(item, config)
    }

    Components.values.filter(inactiveTabs.contains).foreach { c =>
      add(c.name, c.function)
    }
  }

  EventBus.activeTabs.subscribe { tabs =>
    val inactive = Components.values.toSet -- tabs
    refresh(inactive)
  }
}
