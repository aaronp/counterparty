package contract.js

import org.scalajs.dom
import scalatags.JsDom.tags2
import scalatags.JsDom.all._
import org.scalajs.dom.document
import org.scalajs.dom.HTMLLIElement

object Material:

  enum ActiveTab:
    case Flow, Scenarios, Interactive, Diff, REST

end Material

case class Material() {

  import Material.*

  private val tabListItemPairs: Seq[(ActiveTab, HTMLLIElement)] = ActiveTab.values.map { t =>
    val tabListItem =
      li(cls := (if t == ActiveTab.Flow then "active" else ""), a(href := "#", t.toString())).render
    tabListItem.onclick = (e: dom.MouseEvent) => {
      e.preventDefault()
      refreshTabs(t)
    }
    t -> tabListItem
  }

  private val tabListItems = tabListItemPairs.map(_._2)

  private val tabListItemsByTab = tabListItemPairs.toMap

  private def bodyForTab(t: ActiveTab) = t match {
    case ActiveTab.Flow =>
      div("Flow, baby!").render
    case ActiveTab.Scenarios =>
      div("Scenarios, baby!").render
    case ActiveTab.Interactive =>
      div("Interactive, baby!").render
    case ActiveTab.REST =>
      div("REST, baby!").render
    case ActiveTab.Diff =>
      div("Diff, baby!").render
  }
  private def refreshTabs(t: ActiveTab) = {
    tabListItemPairs.foreach {
      case (`t`, tabListItem) => tabListItem.classList.add("active")
      case (_, tabListItem)   => tabListItem.classList.remove("active")
    }
    contentWrapper.innerHTML = ""
    contentWrapper.appendChild(bodyForTab(t))
  }

  private val contentWrapper = div().render

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
        h1("Welcome to the App"),
        contentWrapper
      )
    )
  }

// Render the content into the HTML element with id "root"
//   document.getElementById("root").innerHTML = content.render

}
