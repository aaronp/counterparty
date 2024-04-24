package contract.js

import org.scalajs.dom
import scalatags.JsDom.all.*
import scalatags.JsDom.implicits.given

import scala.concurrent.duration.{*, given}
import org.scalajs.dom.*
import scalatags.JsDom.svgAttrs.{height, style, width, xmlns}
import scalatags.JsDom.svgTags.svg
import counterparties.buildinfo.BuildInfo.version

object Scaffold {

  type Label = String

  def apply(tabs: (Label, org.scalajs.dom.Node)*) = {
    val (options, bodies) = tabs.zipWithIndex.map { case ((label, content), i) =>
      val opt = option(value := s"tab-option-$i", label).render
      val body = div(
        id      := s"tab-$i",
        display := (if i == 0 then "block" else "none")
      )(content)

      (opt.render, body.render)
    }.unzip

    val selector = select(
      id := "tabSelector",
      options
    ).render

    val footer = div(selector.selectedIndex).render

    selector.onchange = (e: dom.Event) => {
      bodies.zipWithIndex.foreach {
        case (tab, i) if i == selector.selectedIndex => tab.style.display = "block"
        case (tab, _)                                => tab.style.display = "none"
      }
      footer.innerHTML = s"Selected: ${selector.selectedIndex}"
    }

    div(style := "display: flex; flex-direction: column; align-items: flex-start;")(
      selector,
      div(bodies),
      footer
    ).render
  }

}
