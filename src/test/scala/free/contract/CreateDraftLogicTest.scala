package free.contract

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import zio.*

import java.util.UUID
import scala.collection.mutable

/**
 * This is just one example of more traditional "mock" testing.
 *
 * This test provides in-memory services (counterparty services, database services) within a 'TestEnv'
 * class.
 *
 * That class can then turn the CreateDraftLogic into
 */
class CreateDraftLogicTest extends AnyWordSpec with Matchers {

  "CreateDraftLogic" should {
    val draft = DraftContract.testData
    s"be able to create $draft" in {
      val testEnv = InMemoryEnv()
      
      val result = run(testEnv.test(draft))

      val stableId = DraftContractId(UUID.fromString("fcd9af7e-a3d2-3422-a140-5d8dfd6128ba"))

      // our assertions: prove our (fake) DB and counterparty services actually got invoked with the right arguments
      val expectedResponse: CreateDraftResponse = CreateDraftResponse(
        CounterpartyRef(s"Ack-$stableId"),
        CounterpartyRef(s"Ack-$stableId")
      )
      result shouldBe expectedResponse
      testEnv.asMap("counterpartyA") shouldBe List(Contract(draft, stableId))
      testEnv.asMap("counterpartyA") shouldBe List(Contract(draft, stableId))
      testEnv.asMap("db") shouldBe Map(stableId -> draft)
      testEnv.asMap("logs") shouldBe List(s"Saving draft $draft", s"Saved draft $stableId", s"Returning $expectedResponse")
    }
  }

  // convenience method to run a Task
  private def run[A](job: Task[A]): A = Unsafe.unsafe { implicit unsafe =>
    Runtime.default.unsafe.run(job).getOrThrowFiberFailure()
  }
}

