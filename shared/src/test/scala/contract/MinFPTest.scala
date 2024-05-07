package contract

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MinFPTest extends AnyWordSpec with Matchers {

  "extension asResultTraced" should {
    "work for operations which throw an exception" in {
      val resultTraced = sys.error("Bang").asResultTraced(Actor.person("some", "test"))

      val Left(result) = resultTraced.task.either.execOrThrow()
      result.getMessage shouldBe "Bang"
    }
  }
}
