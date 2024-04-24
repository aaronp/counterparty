package free.examples.etl.ui

import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.{MouseEvent, document, window}

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import scala.scalajs.js.annotation.*
//import scala.scalajs.js.annotation.*

//import scalatags.Text.all.*
import scalatags.JsDom.all.*

import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.{MouseEvent, window}
import scalatags.JsDom.all.{`class`, `for`, `type`, button, div, form, h1, input, s}

import java.time.{ZoneId, ZonedDateTime}

case class MessagePage() {
  val msgId     = newInput("Message Id", "x")
  val timestamp = newInput("Timestamp", "t-1")
  val userId    = newInput("User Id", "foo")

  val refresh = button("Refresh").render
  refresh.onclick = _ => {
    val m = asMessage
//    window.console.log(m.toJson)
//    timestamp.value = m.timestamp.toString
  }

  def timestampValue: ZonedDateTime = parseTime(timestamp.value)
//  def asMessage = UpstreamMessage(messageId = msgId.value, userId = userId.value, timestamp = timestampValue)
  def asMessage = ???

  val render: Div = {
    div(`style` := "flex-grow: 1")(
      h1("Message"),
      div(`class` := "container")(
        form(
          newRow(msgId),
          newRow(timestamp),
          newRow(userId)
        ),
        refresh
      )
    ).render
  }
}
