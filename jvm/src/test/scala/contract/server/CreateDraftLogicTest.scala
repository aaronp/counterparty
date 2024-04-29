package contract.server

import contract.server.execOrThrow
import contract.handler.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

/** This is just one example of more traditional "mock" testing.
  *
  * This test provides in-memory services (counterparty services, database services) within a
  * 'TestEnv' class.
  *
  * That class can then turn the CreateDraftLogic into
  */
class CreateDraftLogicTest extends AnyWordSpec with Matchers {

  "CreateDraftLogic" should {
    val draft = DraftContract.testData
    s"be able to create $draft" in {
      val testEnv = CreateDraftHandler.InMemory()

      val result = testEnv.run(draft).execOrThrow()

      val stableId = DraftContractId("id-0")

      // our assertions: prove our (fake) DB and counterparty services actually got invoked with the right arguments
      val expectedResponse: CreateDraftResponse = CreateDraftResponse(
        Option(s"Ack-$stableId"),
        Option(s"Ack-$stableId")
      )
      result shouldBe expectedResponse
      testEnv.asMap("counterpartyA") shouldBe List(Contract(draft, stableId))
      testEnv.asMap("counterpartyA") shouldBe List(Contract(draft, stableId))
      testEnv.asMap("db") shouldBe Map(stableId -> draft)
      testEnv.asMap("logs") shouldBe List(
        s"Saving draft $draft",
        s"Saved draft $stableId",
        s"Returning $expectedResponse"
      )
    }
  }

}
