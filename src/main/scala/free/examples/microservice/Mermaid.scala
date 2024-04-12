package free.examples.microservice

import _root_.free.examples.microservice.CreateDraftLogic.*
import _root_.free.examples.{*, given}
import free.*

import scala.annotation.targetName

@main def generateMermaidDiagram(): Unit = println(Mermaid())

/**
 * Turn an operation into a mermaid diagram
 */
object Mermaid {

  object actors {
    val Svc = "ContractService"
    val DB = "Database"
    val CtrPtyA = "CounterpartyA"
    val CtrPtyB = "CounterpartyB"

    def all = List(Svc, DB, CtrPtyA, CtrPtyB)
  }
  type Call = String
  extension (call: Call) {
    def asState[A](result: A) = State.combine[Calls, A](call :: Nil, result)
  }

  type Calls = List[Call]
  extension (calls: Calls)
    def asMermaidDiagram: String = {
      (actors.all.map(p => s"participant $p") ++ calls).mkString("\n")
    }

  given Semigroup[Calls] with
    def combine(a: Calls, b: Calls) = a ++ b

  import actors.*
  def mermaidInterpreter[A]: CreateDraftLogic[A] => State[Calls, A] = (_: CreateDraftLogic[A]) match {
    case StoreDraftInDatabase(draft) =>
      val result = DraftContractId.create()
      s"$Svc ->> $DB".asState(result)
    case NotifyCounterpartyA(contract) =>
      val result = CounterpartyRef(s"notified party A of ${contract.id}")
      s"$Svc ->> $CtrPtyA : notify of ${contract.id}".asState(result)
    case NotifyCounterpartyB(contract) =>
      val result = CounterpartyRef(s"notified party B of ${contract.id}")
      s"$Svc ->> $CtrPtyB : notify of ${contract.id}".asState(result)
    case LogMessage(msg) =>
      s"Note right of $Svc : $msg".asState(())
  }

  type CallsState[A] = State[Calls, A]

  // we need a natural transformation to convert our operation types
  // into a 'BufferState' -- which is something that just keeps a
  // list of the operations called and their responses
  @targetName("createDraftLogicAsBufferState")
  given~>[CreateDraftLogic, CallsState] with
    def apply[A](op: CreateDraftLogic[A]): State[Calls, A] = mermaidInterpreter(op)

  def exampleContract = DraftContract(
    CounterpartyA("counterparty-A"),
    CounterpartyB("counterparty-B"),
    "the terms of the contract",
    "the conditions of the contract"
  )

  def apply(draft: DraftContract = exampleContract): String = {
    val logic = CreateDraftLogic(draft)

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[CallsState]

    // .. and then "run" that state by giving it an empty buffer
    run(Nil)._2.reverse.asMermaidDiagram
  }
}
