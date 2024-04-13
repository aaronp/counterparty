package contract

import contract.server.*
import counterparty.service.server.model.{SignDraftContract200Response, SignDraftContractRequest}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * This is just one example of more traditional "mock" testing.
 *
 * That class can then turn the EnactContractLogic into a result
 */
class EnactContractLogicTest extends AnyWordSpec with Matchers {

  "EnactContractLogic" should {

    "be able to sign contracts" in {
      val testEnv = SignContractInMemory()

      val request = SignDraftContractRequest("foo", "bar")
      val result = testEnv.run(request).execOrThrow()

      testEnv.signedContractsForA shouldBe List(CounterpartyRef("foo"))
      testEnv.signedContractsForB shouldBe List(CounterpartyRef("bar"))
      result shouldBe SignDraftContract200Response(true, true)
    }
  }
}

