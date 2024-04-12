package free.examples.microservice

import free.*
import _root_.free.examples.{*, given}

import CreateDraftLogic.*
import scala.annotation.targetName
import scala.util.control.NonFatal

@main def generateMermaidDiagram(): Unit = println(Mermaid())
/**
 * Turn an operation into a mermaid diagram
 */
object Mermaid {

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

//  private val interpreter = (op: CreateDraftLogic[_]) => op match {
//    case result => State.combine[Buffer, Any](Buffer(op, result), result)
//  }

  // we need a natural transformation to convert our operation types
  // into a 'BufferState' -- which is something that just keeps a
  // list of the operations called and their responses
  @targetName("createDraftLogicAsBufferState")
  given~>[CreateDraftLogic, BufferState] with
    def apply[A](op: CreateDraftLogic[A]): BufferState[A] = makeInterpreter[A](PartialFunction.empty)(op)

  def exampleContract = DraftContract(
    CounterpartyA("counterparty-A"),
    CounterpartyB("counterparty-B"),
    "the terms of the contract",
    "the conditions of the contract"
  )

  def apply(draft : DraftContract = exampleContract) = {
    val logic: Free[CreateDraftLogic, CreateDraftResponse] = CreateDraftLogic(draft)

    println(s"logic is\n$logic")

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[BufferState]

    // .. and then "run" that state by giving it an empty buffer
    val buffer: Buffer = run(Buffer(Nil))._2

    println(buffer)
  }
}
