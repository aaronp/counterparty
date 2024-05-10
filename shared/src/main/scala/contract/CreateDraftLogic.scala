package contract

import contract.model.*
import support.{given, *}

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
  def apply(draft: DraftContract): Program[CreateDraftLogic, Either[String, CreateDraftResponse]] =
    validateDraft(draft) match {
      case Some(err) =>
        for log <- LogMessage(s"Validation failed: $err").asProgram
        yield Left(err)
      case None =>
        for
          _  <- LogMessage(s"Saving draft $draft").asProgram
          id <- StoreDraftInDatabase(draft).asProgram
          contract = Contract(draft, id)
          _    <- LogMessage(s"Saved draft ${id}").asProgram
          refA <- NotifyCounterpartyA(contract).asProgram
          refB <- NotifyCounterpartyB(contract).asProgram
          response = CreateDraftResponse(Option(refA.code), Option(refB.code))
          _ <- LogMessage(s"Returning $response").asProgram
        yield Right(response)
    }

  private def validateDraft(draft: DraftContract) = {
    if draft.conditions.trim.isEmpty() then {
      Some("Draft has no conditions")
    } else if draft.terms.trim.isEmpty() then {
      Some("Draft has no terms")
    } else if draft.firstCounterparty.trim.isEmpty() then {
      Some("The first counterparty is empty")
    } else if draft.secondCounterparty.trim.isEmpty() then {
      Some("The second counterparty is empty")
    } else {
      None
    }
  }
