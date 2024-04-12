package free.examples


import java.time.LocalDateTime
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
package object microservice {

  // for readability (and type-checking, and flexibility), we'll create some type-aliases.
  // this will help the compiler enforce correctness rather than us having to write additional tests

  // ... and let's not accidentally mix up our counterparties!
  opaque type CounterpartyA = String
  opaque type CounterpartyB = String

  opaque type CounterpartyRef = String

  opaque type DraftContractId = UUID

  // just to be clear what our errors are, rather than just strings
  type Error = String
  // we may want to have separate types for the user input and our contract.
  // We'll create an alias here so it looks like we've done this, but it's just a pass-through
  opaque type Contract = DraftContract
  object Contract {
    def apply(draft :DraftContract) : Error | Contract = draft
  }


  // the response from creating a draft contract
  final case class CreateDraftResponse(firstCounterpartyRef : CounterpartyRef,
                                       secondCounterpartyRef : CounterpartyRef)

  // Our user data: just a noddy data structure representing some of the things in a contract
  final case class DraftContract(firstCounterparty: CounterpartyA,
                                 secondCounterparty: CounterpartyB,
                                 terms: String,
                                 conditions: String,
                                 startDate: LocalDateTime)

  // counterparties may not sign the contract, opting instead to provide a reason
  type NotSignedReason = String
  final case class SignContractResponse(firstCounterparty : CounterpartyRef | NotSignedReason,
                                        secondCounterparty : CounterpartyRef | NotSignedReason)


}
