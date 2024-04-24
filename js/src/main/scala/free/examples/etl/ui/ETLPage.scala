package free.examples.etl.ui

import free.examples.etl.*
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.{MouseEvent, document, html, window}
import scalatags.JsDom.all.*

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import scala.scalajs.js.annotation.*

@JSExportTopLevel("ETLPage")
case class ETLPage(containerId: String) {

  val targetDiv = document.getElementById(containerId).asInstanceOf[Div]

  val msgPage = MessagePage()
  val cfgPage = ConfigPage()

  val dbTable = Table("db", Row("User Id", "User Name"))
  dbTable.addRow(Row("foo", "BAR"))
  dbTable.addRow(Row("fizz", "Buzz"))
  val stateTable = Table("state", Row("User Id", "Last Received"))

  val results = div().render

  val testButton =
    button(`style` := "width:100%;height:90px;font-size:20pt")("send test message").render

  def lookupUser(userId: Any): Option[String] = {
//    val rows = dbTable.rowsForCells()
//    rows.collectFirst {
//      case row if row.values.headOption.exists(_.trim == userId) => row.values.last
//    }
    ???
  }
  def upsertStateRow(userId: String, now: String) = {
    stateTable.upsertRow(Row(userId, now), 0)
  }

//  def makeInterpreter(config: Config) = {
//    val stateDB: InMemoryDB = InMemoryDB()
//    stateTable.rowsForCells().foreach { case Row(Seq(key, value)) =>
//      stateDB.update(key, parseTime(value))
//    }
//    val lookup: PartialFunction[Operations[_], _] = {
//      case Operations.FindUser(userId) => lookupUser(userId)
//      case Operations.SaveEvent(msg, now) =>
//        stateDB.update(msg.userId, now)
//        upsertStateRow(msg.userId, now.toString)
//        ()
//    }
//    TestInterpreter(stateDB, config, lookup)
//  }

//  def runTest(message: UpstreamMessage, config: Config): html.Table = {
  def runTest(message: Any, config: Any): html.Table = {
    val interpreter = ??? // makeInterpreter(config)
    val buffer      = ??? // interpreter.testMessage(message)
    tableFromResults(buffer)
  }

  def tableFromResults(buffer: Buffer) = {
    val rows = buffer.steps.map { (input, output) =>
      val out = output.toString match {
        case "undefined" => ""
        case value       => value
      }
      tr(td(input.toString), td(out))
    }
    val header = tr(td("INPUT"), td("RESULT"))
    table(header +: rows).render
  }

  testButton.onclick = _ => {
    val commands = runTest(msgPage.asMessage, cfgPage.asConfig)
    results.innerHTML = ""
    results.appendChild(commands)
  }

  val page = div(
    div(`class` := "flex-container")(msgPage.render, cfgPage.render),
    div(`class` := "flex-container")(
      div(`style` := "flex-grow: 1")(h2("Database"), dbTable.render),
      div(`style` := "flex: 0 0 800px")(h2("State"), stateTable.render)
    ),
    div(`class` := "flex-action")(
      div(`style` := "flex-grow: 2")(testButton)
    ),
    results
  ).render

  targetDiv.appendChild(page)
}
