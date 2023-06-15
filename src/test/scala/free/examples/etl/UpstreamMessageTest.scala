package free.examples.etl

import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import util.*
import java.time.{ZoneId, ZonedDateTime}

class UpstreamMessageTest extends AnyWordSpec with Matchers {

  "UpstreamMessage.toJson" should {
    "produce valid json" in {
      val expected = UpstreamMessage("foo", "bar", UpstreamMessageTest.timestamp)
      val Success(backAgain) = UpstreamMessage.fromJson(expected.toJson)
      backAgain shouldBe expected
    }
  }
}


object UpstreamMessageTest:
  def timestamp = ZonedDateTime.of(2000,2,3,4,5,6,7000000, ZoneId.of("UTC"))