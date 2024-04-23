package contract.interactive

import contract.interactive.*
import contract.interactive.ui.*
import org.scalajs.dom
import scalatags.JsDom.all.*
import scalatags.JsDom.implicits.given

import scala.concurrent.duration.{*, given}

@main
def layout(): Unit = {
  val actors = List(
    Actor.person("counterparty-A", "Alice"),
    Actor.person("counterparty-A", "Alice's Lawyer"),
    Actor.database("counterparty-service", "MongoDB"),
    Actor.queue("counterparty-service", "Notification Queue"),
    Actor.service("counterparty-service", "Counterparty Service"),
    Actor.service("counterparty-service", "Notification ETL"),
    Actor.email("counterparty-service", "Email Service"),
    Actor.person("counterparty-B", "Bob")
  )
  val List(alice, lawyer, mongo, queue, service, etl, email, bob) = actors
  // val List(alice, bob) = actors
  val messages: List[SendMessage] = List(
    SendMessage(alice, service, 1234, 1.seconds, ujson.Obj("hello" -> "world")),
    SendMessage(alice, bob, 1234, 1.seconds, ujson.Obj("hello" -> "world")),
    SendMessage(service, mongo, 2345, 2.seconds, ujson.Obj("hello" -> "world")),
    SendMessage(alice, service, 1234, 10.seconds, ujson.Obj("go" -> "there"))
  )

  val config            = Config.default()
  val systemActorLayout = SystemActorsLayout(actors.toSet, config)

  val tooltip = div(
    id := "tooltip",
    style := "display: none; position: absolute; width:500px; height: 500px; background: lightgrey; border: 1px solid black; margin: 5px; padding: 5px; text-align:left"
  ).render
  def onClick(e: dom.MouseEvent, messageOpt: Option[MessageLayout.RenderedMessage]) =
    messageOpt match {
      case Some(msg) =>
        // TODO: the message flashes/jumps if the div goes off the screen and introduces scrollbars
        tooltip.style.left = e.clientX + 10 + "px"
        tooltip.style.top = e.clientY + 10 + "px"
        tooltip.style.display = "flex"
        tooltip.innerHTML = ""

        def line(label: String, value: String) = div(
          scalatags.JsDom.all
            .span(style := "width: 50em; display: inline-block; font-weight: bold")(label),
          scalatags.JsDom.all.span(style := "display: inline-block;")(value)
        )

        tooltip.append(
          div(style := "text-align:left")(
            h2("Message"),
            line("From:", msg.msgFromString),
            line("To:", msg.msgToString),
            line("At:", s"${msg.timestamp}"),
            line("Took:", s"${msg.duration.toMillis}ms"),
            h3("Message:"),
            p(msg.message.message.render(2))
          ).render
        )

      case None =>
        tooltip.style.display = "none"
    }

  val messagesLayout = MessageLayout(messages, systemActorLayout.positionByActor, onClick)

  val app = dom.document.querySelector("#app")
  app.innerHTML = ""
  app.appendChild(
    div(
      tooltip,
      InteractiveMessageSvgComponent(systemActorLayout, messagesLayout, config).render
    ).render
  )

}
