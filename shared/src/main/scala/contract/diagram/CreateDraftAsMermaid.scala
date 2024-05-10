package contract.diagram

import contract.CreateDraftLogic.{
  LogMessage,
  NotifyCounterpartyA,
  NotifyCounterpartyB,
  StoreDraftInDatabase
}
import contract.{CounterpartyRef, CreateDraftLogic, DraftContract, DraftContractId}
import support.{State, ~>}

import scala.annotation.targetName

object CreateDraftAsMermaid {

  /** An interpreter which turns our commands into mermaid syntax (built up in a State monad)
    */
  import Participants.*

  // we need a natural transformation to convert our operation types
  // into a 'BufferState' -- which is something that just keeps a
  // list of the operations called and their responses
  @targetName("createDraftLogicAsMermaid")
  given ~>[CreateDraftLogic, CallsState] with
    def apply[A](op: CreateDraftLogic[A]): State[Calls, A] = op match {
      case StoreDraftInDatabase(draft) =>
        val result = DraftContractId(s"contract for ${draft.terms}")
        List(
          s"$ContractService->>$Database: save draft",
          s"$Database-->>$ContractService: returns $result"
        ).asState(result)
      case NotifyCounterpartyA(contract) =>
        val result = CounterpartyRef(s"notified party A of ${contract.id}")
        List(
          s"$ContractService->>$CounterpartyA : notify of ${contract.id}",
          s"$CounterpartyA-->>$ContractService : $result"
        ).asState(result)
      case NotifyCounterpartyB(contract) =>
        val result = CounterpartyRef(s"notified party B of ${contract.id}")
        List(
          s"$ContractService->>$CounterpartyB : notify of ${contract.id}",
          s"$CounterpartyB-->>$ContractService : $result"
        ).asState(result)
      case LogMessage(msg) =>
        s"$ContractService->>$ContractService : log ${msg.take(18)}...".asState(())
    }

  /** function which turns the request into a mermaid sequence diagram. see
    * https://mermaid.js.org/syntax/sequenceDiagram.html
    * @param draft
    *   the request
    * @return
    *   a mermaid markup diagram
    */
  def apply(draft: DraftContract = DraftContract.testData): String = {
    val logic = CreateDraftLogic(draft)

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[CallsState]

    // .. and then "run" that state by giving it an empty buffer
    val participants = Seq(
      Participants.ContractService,
      Participants.Database,
      Participants.CounterpartyA,
      Participants.CounterpartyB
    )
    run(Nil)._2.reverse.asMermaidDiagram(participants.map(_.toString))
  }
}
