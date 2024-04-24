package contract.diagram

import contract.*
import contract.model.*

import scala.annotation.targetName

object EnactContractAsMermaid extends EnactContract {

  import EnactContractLogic.*
  override type Async[_] = String

  def testData: SignContract = SignDraftContractRequest("foo", "bar")

  /** An interpreter which turns our commands into mermaid syntax (built up in a State monad)
    */
  import Participants.*

  @targetName("enactContractAsMermaid")
  given ~>[EnactContractLogic, CallsState] with
    def apply[A](op: EnactContractLogic[A]): State[Calls, A] = op match {
      case RequestSignatureA(ref) =>
        s"$ContractService->>+$CounterpartyA: request signature for $ref".asState(
          CounterpartyA.toString
        )
      case RequestSignatureB(ref) =>
        s"$ContractService->>+$CounterpartyB: request signature for $ref".asState(
          CounterpartyB.toString
        )
      case Wait(counterpartyName) =>
        s"$counterpartyName->>-$ContractService: send response".asState(
          counterpartyName.asInstanceOf[A]
        )
      case LogMessage(msg) =>
        s"$ContractService->>$ContractService : log ${msg.take(18)}...".asState(())
    }

  /** function which turns the request into a mermaid sequence diagram. see
    * https://mermaid.js.org/syntax/sequenceDiagram.html
    * @param sign
    *   the request
    * @return
    *   a mermaid markup diagram
    */
  def apply(sign: SignContract = testData): String = {
    val logic = EnactContractLogic(sign)

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[CallsState]

    // .. and then "run" that state by giving it an empty buffer
    val participants =
      Seq(Participants.ContractService, Participants.CounterpartyA, Participants.CounterpartyB)
    run(Nil)._2.reverse.asMermaidDiagram(participants.map(_.toString))
  }
}
