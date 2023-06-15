package free.examples.etl

import _root_.free.*
import _root_.free.examples.{Buffer, given, *}
import Operations.{Log, *}
import _root_.free.examples.etl.ui.TestInterpreter
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import concurrent.duration.{*, given}
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import scala.util.*

class LogicTest extends AnyWordSpec with Matchers {

  def testMessage(input: UpstreamMessage, interpreter: TestInterpreter = TestInterpreter()): Buffer = interpreter.testMessage(input)

  def testBytes(inputBytes: Array[Byte], interpreter: TestInterpreter = TestInterpreter()): Buffer = interpreter.testBytes(inputBytes)

  "Logic" should {


    "ignore messages which seemingly appear in the future" in {

      val message = UpstreamMessage("msg-id", "userA", UpstreamMessageTest.timestamp)
      val List(_, (ParseMessage(_), Success(msg)), (GetConfig(), config), (Log(ignoreMsg, false), ())) = testMessage(message).steps

      msg shouldBe message
      ignoreMsg shouldBe "ignoring message for user userA seemingly sent in the future. now=0001-02-03T04:05Z[UTC] and msg.timestamp is 2000-02-03T04:05:06.007Z[UTC]"
    }

    "process a message" in {

      val interpreter = TestInterpreter().withConfig(Config(1.seconds, 10.seconds, UpstreamMessageTest.timestamp))
      val message = UpstreamMessage("msg-id", "userA", UpstreamMessageTest.timestamp)
      val steps1 = testMessage(message, interpreter)
      val steps2 = testMessage(message, interpreter)

      println(steps1)
      println("-" * 20)
      println(steps2)
    }
    "fail for invalid bytes" in {
      val List(startLogMsg, parsingEvent, failEvent) = testBytes(Array.empty).steps

      startLogMsg shouldBe (Log("handling message", false), ())

      val (ParseMessage(data), Failure(parseError)) = parsingEvent
      parseError.getMessage should startWith("Error parsing json")

      val (Log(failMsg, true), ()) = failEvent
      failMsg should startWith ("failed to parse message")
    }
  }


}
