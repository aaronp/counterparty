package contract

import contract.model.*

/** An 'DraftOperation' represent the actions (and the inputs/outputs) the business logic will need
  * in order to interact with the outside world (users, databases, other services)
  *
  * @tparam A
  *   this is the generic result type of each operation
  */
enum CreateDraftLogic[A]:
  case StoreDraftInDatabase(draft: DraftContract) extends CreateDraftLogic[DraftContractId]
  case NotifyCounterpartyA(contract: Contract)    extends CreateDraftLogic[CounterpartyRef]
  case NotifyCounterpartyB(contract: Contract)    extends CreateDraftLogic[CounterpartyRef]
  case LogMessage(message: String)                extends CreateDraftLogic[Unit]

object CreateDraftLogic:

  /** This is our business logic (i.e. the control flow) for creating draft contracts
    *
    * @param draft
    *   the draft contract
    * @return
    *   the business logic for creating a draft contract
    */
  def apply(draft: DraftContract) = for {
    _  <- LogMessage(s"Saving draft $draft").asProgram
    id <- StoreDraftInDatabase(draft).asProgram
    _  <- LogMessage(s"Saved draft ${id}").asProgram
    contract = Contract(draft, id)
    refA <- NotifyCounterpartyA(contract).asProgram
    refB <- NotifyCounterpartyB(contract).asProgram
    response = CreateDraftResponse(Option(refA.code), Option(refB.code))
    _ <- LogMessage(s"Returning $response").asProgram
  } yield response
