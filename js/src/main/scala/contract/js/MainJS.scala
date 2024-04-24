package contract.js

import contract.js.mermaid.*
import contract.js.interactive.*
import contract.js.interactive.ui.*
import org.scalajs.dom
import scalatags.JsDom.all.*
import scalatags.JsDom.implicits.given

import scala.concurrent.duration.{*, given}

@main
def layout(): Unit = {

  val app = dom.document.querySelector("#app")
  app.innerHTML = ""
  // app.appendChild(Material().content.render)
  app.appendChild(MermaidPage().element)

}
