package contract

import org.scalajs.dom.html.Div
import org.scalajs.dom.{MouseEvent, window}
import org.scalajs.dom.html.{Div, Input}
import scalatags.JsDom.all.*

import java.time.format.DateTimeFormatter
import concurrent.duration.*
import java.time.{ZoneId, ZonedDateTime}

import contract.js.interactive.Json
import upickle.default.{ReadWriter => RW, macroRW}
import upickle.default.*
import ujson.Value

package object js {

  /** The tabs in this UI
    */
  enum ActiveTab:
    case Flow, Scenarios, Interactive, Diff, REST

  /** Represents a named scenario
    *
    * @param name
    *   the friendly name of this scenario
    * @param description
    *   the description
    * @param input
    *   the input into this scenario
    * @param lastResult
    *   the result from the most-recent run of this scenario
    */
  case class TestScenario(
      name: String,
      description: String,
      input: Json = ujson.Null,
      lastResult: Option[TestResult] = None
  ) {
    def asJson: Value = writeJs(this)
  }

  object TestScenario {
    given readWriter: RW[TestScenario]                      = macroRW
    def fromJson(jason: Json): TestScenario                 = read[TestScenario](jason)
    def mapFromJson(jason: Json): Map[String, TestScenario] = read[Map[String, TestScenario]](jason)
  }

  // for each stack in a test call frame, there is an input and an output
  type StackElement = (Json, Json)

  /** The output of running a test scenario
    */
  case class TestResult(callstack: Seq[StackElement] = Nil, result: Json = ujson.Null) {
    def asJson: String = write(this)
  }

  object TestResult {
    given readWriter: RW[TestResult]      = macroRW
    def fromJson(jason: Json): TestResult = read[TestResult](jason)
  }

}
