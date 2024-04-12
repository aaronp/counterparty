import counterparty.service.server.model.CreateContractRequest

import java.util.UUID

/**
 * In this example, we're representing the flow of a microservice responsible
 * for contracts.
 *
 * As it's an example, this is just a general concept, but it's doing things typical
 * of many microservices:
 * $ Gets some instruction from the user
 * $ Saves some data, perhaps getting back an ID
 * $ Notifying some other services
 *
 * More specifically, this example service will allow users to create draft
 * contracts. The draft contact is sent to two different endpoints
 * (one for each counterparty).
 *
 * Those drafts can then be turned into real contracts by having each counterparty
 * sign.
 *
 * The counter-story to this example is an imperative approach, potentially storing state between calls.
 * One example might look like this (I've seen this in the real world):
 *
 * {{{
 *   public Response logic(DraftContract draftContract) {
 *     createDraftContract(userData);
 *      saveContract();
 *     notifyCounterpartyA();
 *     notifyCounterpartyB();
 *     return makeDraftResponse();
 *   }
 * }}}
 *
 * That may look sensible to a layman -- after all, it lists the steps we're doing to do, and
 * seems pretty readable.
 *
 * But it has some problems -- mostly in that each step relies on "side-effects" ... keeping
 * track of state between calls.
 *
 * For example, we can freely re-order or emit certain steps, and the program will still compile and run:
 *
 *
 * {{{
 *   public Response logic(DraftContract draftContract) {
 *     // we can re-order the steps to do something totally nonsensical, like notifying counterparties
 *     // before even saving the contract
 *     notifyCounterpartyA();
 *     notifyCounterpartyB();
 *     // saveContract(); <-- let's not do this
 *     createDraftContract(userData);
 *     return makeDraftResponse();
 *   }
 * }}}
 *
 */
package object contract {

  // for readability (and type-checking, and flexibility), we'll create some type-aliases.
  // this will help the compiler enforce correctness rather than us having to write additional tests

  opaque type CounterpartyRef = String

  object CounterpartyRef:
    def apply(ref: String): CounterpartyRef = ref

  extension (ref :CounterpartyRef)
    def code: String = ref.toString

  opaque type DraftContractId = UUID

  object DraftContractId:
    def create(): DraftContractId = UUID.randomUUID()

    def apply(id: String): DraftContractId = apply(UUID.nameUUIDFromBytes(id.getBytes))

    def apply(id: UUID): DraftContractId = id

  final case class Contract(draft: DraftContract, id: DraftContractId)

  // Our user data: just a noddy data structure representing some of the things in a contract
  type DraftContract = CreateContractRequest

  object DraftContract:
    def testData = CreateContractRequest(
      "counterparty-A",
      "counterparty-B",
      "the terms of the contract",
      "the conditions of the contract"
    )

  // counterparties may not sign the contract, opting instead to provide a reason
  type NotSignedReason = String

  final case class SignContractResponse(firstCounterparty: CounterpartyRef | NotSignedReason,
                                        secondCounterparty: CounterpartyRef | NotSignedReason)


}
