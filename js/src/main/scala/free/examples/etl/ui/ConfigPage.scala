package free.examples.etl.ui

import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.{MouseEvent, document, window}

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import scala.scalajs.js.annotation.*
//import scala.scalajs.js.annotation.*

//import scalatags.Text.all.*
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.{MouseEvent, window}
import scalatags.JsDom.all.*

import java.time.{ZoneId, ZonedDateTime}

case class ConfigPage() {
  val debounceDuration = newInput("Debounce duration", "3s")
  val dropDuration     = newInput("Drop after duration", "1m")
  val clock            = newInput("Current Time", "now")

  val refresh = button("Refresh").render
  refresh.onclick = _ => {
//    clock.value = asConfig.now.toString
  }

  def asConfig = ???
//    Config(
//    debounceTimeout = parseDuration(debounceDuration.value),
//    messageTimeout = parseDuration(dropDuration.value),
//    now = parseTime(clock.value)
//  )

  val render: Div = {
    div(`style` := "flex: 0 0 800px")(
      h1("Config"),
      div(`class` := "container")(
        form(
          newRow(debounceDuration),
          newRow(dropDuration),
          newRow(clock)
        ),
        refresh
      )
    ).render
  }
}
