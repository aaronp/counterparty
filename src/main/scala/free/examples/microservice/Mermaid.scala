package free.examples.microservice

import _root_.free.examples.{*, given}
import free.*
import _root_.free.examples.microservice.CreateDraftLogic.*

import scala.annotation.targetName

@main def generateMermaidDiagram(): Unit = println(Mermaid())
/**
 * Turn an operation into a mermaid diagram
 */
object Mermaid {

  case class Call(from: String, action :String, result : String) {
    def asState[A](result: A) = State.combine[Calls, A](this :: Nil, result)
  }

  type Calls = List[Call]
  extension (calls : Calls)
    def asMermaidDiagram : String = {
      val participants: List[String] = calls.flatMap {
        case Call(from, _, to) => List(from, to)
      }.distinct

      val declarations = participants.sorted.map(p => s"participant $p")

      val flow: List[String] = calls.map {
        case Call(from, result, to) => s"$from ->> $to: $result"
      }

      (declarations ++ flow).mkString("\n")
    }

  given Semigroup[Calls] with
    def combine(a: Calls, b: Calls) = a ++ b

  def makeInterpreter[A](pf: PartialFunction[CreateDraftLogic[A], A]): CreateDraftLogic[A] => State[Buffer, A] = {
    val handler = (_: CreateDraftLogic[A]) match {
      case op if pf.isDefinedAt(op) => pf(op)
      // the default cases:
      case StoreDraftInDatabase(draft) => DraftContractId.create()
      case NotifyCounterpartyA(contract) => CounterpartyRef(s"notified party A of ${contract.id}")
      case NotifyCounterpartyB(contract) => CounterpartyRef(s"notified party B of ${contract.id}")
      case LogMessage(msg) => ()
    }

    (op: CreateDraftLogic[A]) => {
      handler(op) match {
        case result: A => State.combine[Buffer, A](Buffer(op, result), result)
      }
    }
  }

  def mermaidInterpreter[A]: CreateDraftLogic[A] => State[Calls, A] = (_: CreateDraftLogic[A]) match {
      case StoreDraftInDatabase(draft) =>
        val result = DraftContractId.create()
        Call("ContractService", result.toString, "Database").asState(result)
      case NotifyCounterpartyA(contract) =>
        val result = CounterpartyRef(s"notified party A of ${contract.id}")
        Call("ContractService", result.toString, "CounterpartyA").asState(result)
      case NotifyCounterpartyB(contract) =>
        val result = CounterpartyRef(s"notified party B of ${contract.id}")
        Call("ContractService", result.toString, "CounterpartyB").asState(result)
      case LogMessage(msg) =>
        Call("ContractService", msg, "Logger").asState(())
    }

  type CallsState[A] = State[Calls, A]
  // we need a natural transformation to convert our operation types
  // into a 'BufferState' -- which is something that just keeps a
  // list of the operations called and their responses
  @targetName("createDraftLogicAsBufferState")
  given~>[CreateDraftLogic, CallsState] with
//    def apply[A](op: CreateDraftLogic[A]): BufferState[A] = makeInterpreter[A](PartialFunction.empty)(op)
    def apply[A](op: CreateDraftLogic[A]): State[Calls, A] = mermaidInterpreter(op)

  def exampleContract = DraftContract(
    CounterpartyA("counterparty-A"),
    CounterpartyB("counterparty-B"),
    "the terms of the contract",
    "the conditions of the contract"
  )

  def apply(draft : DraftContract = exampleContract): String = {
    val logic = CreateDraftLogic(draft)

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[CallsState]

    // .. and then "run" that state by giving it an empty buffer
    run(Nil)._2.reverse.asMermaidDiagram
  }
}
