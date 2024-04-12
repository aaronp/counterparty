package free.contract

import free.{*, given}

/**
 * An 'DraftOperation' represent the actions (and the inputs/outputs)
 * the business logic will need in order to interact with the outside world (users, databases, other services)
 *
 * @tparam A this is the generic result type of each operation
 */
enum CreateDraftLogic[A]:
  case StoreDraftInDatabase(draft: DraftContract) extends CreateDraftLogic[DraftContractId]
  case NotifyCounterpartyA(contract: Contract) extends CreateDraftLogic[CounterpartyRef]
  case NotifyCounterpartyB(contract: Contract) extends CreateDraftLogic[CounterpartyRef]
  case LogMessage(message: String) extends CreateDraftLogic[Unit]

object CreateDraftLogic:

  /**
   * This is our business logic (i.e. the control flow) for creating draft contracts
   * @param draft the draft contract
   * @return the business logic for creating a draft contract
   */
  def apply(draft: DraftContract) : Free[CreateDraftLogic, CreateDraftResponse] = for {
    _ <- LogMessage(s"Saving draft $draft").freeM
    id <- StoreDraftInDatabase(draft).freeM
    _ <- LogMessage(s"Saved draft ${id}").freeM
    contract = Contract(draft, id)
    refA <- NotifyCounterpartyA(contract).freeM
    refB <- NotifyCounterpartyB(contract).freeM
    response = CreateDraftResponse(refA, refB)
    _ <- LogMessage(s"Returning $response").freeM
  } yield response