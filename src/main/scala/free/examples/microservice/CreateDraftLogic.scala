package free.examples.microservice


/**
 * An 'DraftOperation' represent the actions (and the inputs/outputs)
 * the business logic will need in order to interact with the outside world (users, databases, other services)
 *
 * @tparam A this is the generic result type of each operation
 */
enum CreateDraftLogic[A]:
  case CreateDraft(draft: DraftContract) extends CreateDraftLogic[CreateDraftResponse]
  case StoreDraftInDatabase(draft: DraftContract) extends CreateDraftLogic[DraftContractId]
  case NotifyCounterpartyA(draft: Contract) extends CreateDraftLogic[CounterpartyRef]
  case NotifyCounterpartyB(draft: Contract) extends CreateDraftLogic[CounterpartyRef]
  case LogMessage(message: String) extends CreateDraftLogic[Unit]

object CreateDraftLogic {

}