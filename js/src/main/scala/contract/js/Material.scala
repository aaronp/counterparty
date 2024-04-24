package contract.js

import org.scalajs.dom
import scalatags.JsDom.tags2
import scalatags.JsDom.all._
import org.scalajs.dom.document
import org.scalajs.dom.HTMLLIElement
import contract.js.scenarios.ScenariosElement

object Material {
  def initSelect(): Unit = scalajs.js.Dynamic.global.M.FormSelect.init {
    document.querySelectorAll("select")
  }

  def initModal() = scalajs.js.Dynamic.global.M.Modal.init {
    document.querySelectorAll(".modal")
  }

  def initTextArea() = scalajs.js.Dynamic.global.M.textarea.init {
    document.querySelectorAll("textarea")
  }

  def init() = {
    initSelect()
    initModal()
  }

}

/** This is the base of our application.
  */
case class Material() {

  // see https://materializecss.com/select.html  about having to initialise
  // we have to initialise some garbase. Love web development 🫶🫶🫶
  locally {
    dom.document.addEventListener(
      "DOMContentLoaded",
      (e: dom.Event) => Material.init()
    )
  }

  private val tabListItemPairs: Seq[(ActiveTab, HTMLLIElement)] = {
    val current = LocalState.currentTab
    ActiveTab.values.map { t =>
      val tabListItem =
        li(cls := (if t == current then "active" else ""), a(href := "#", t.toString())).render
      tabListItem.onclick = (e: dom.MouseEvent) => {
        e.preventDefault()
        refreshTabs(t)
      }
      t -> tabListItem
    }
  }

  private val tabListItems = tabListItemPairs.map(_._2)

  private val tabListItemsByTab = tabListItemPairs.toMap

  private def bodyForTab(t: ActiveTab) = t match {
    case ActiveTab.Flow =>
      div("Flow, baby!").render
    case ActiveTab.Scenarios =>
      ScenariosElement().render
    case ActiveTab.Interactive => TestData.interactive
    case ActiveTab.REST =>
      div("REST, baby!").render
    case ActiveTab.Diff =>
      div("Diff, baby!").render
  }
  private def refreshTabs(t: ActiveTab) = {
    LocalState.currentTab = t
    tabListItemPairs.foreach {
      case (`t`, tabListItem) => tabListItem.classList.add("active")
      case (_, tabListItem)   => tabListItem.classList.remove("active")
    }
    contentWrapper.innerHTML = ""
    contentWrapper.appendChild(bodyForTab(t))

    Material.initTextArea()
  }

  private val contentWrapper = div(bodyForTab(LocalState.currentTab)).render

// Define the content with Scalatags
  val content = {
    div(
      // App Bar (using Materialize Navbar as an example)
      tags2.nav(
        div(
          cls := "nav-wrapper",
          span(style := "margin-left: 2em", cls := "brand-logo", "Contracts"),
          ul(
            style := "margin-right: 2em",
            id    := "nav-mobile",
            cls   := "right hide-on-med-and-down",
            tabListItems
          )
        )
      ),
      // Main content
      div(
        cls := "container",
        contentWrapper
      )
    )
  }

// Render the content into the HTML element with id "root"
//   document.getElementById("root").innerHTML = content.render

}
