package free.examples.microservice

import free.*
import _root_.free.examples.{*, given}

import scala.annotation.targetName
import scala.util.control.NonFatal

@main def generateMermaidDiagram(): Unit = println(Mermaid())
/**
 * Turn an operation into a mermaid diagram
 */
object Mermaid {
  private val interpreter = (op: CreateDraftLogic[_]) => op match {
    case result => State.combine[Buffer, Any](Buffer(op, result), result)
  }

  // we need a natural transformation to convert our operation types
  // into a 'BufferState' -- which is something that just keeps a
  // list of the operations called and their responses
  @targetName("createDraftLogicAsBufferState")
  given~>[CreateDraftLogic, BufferState] with
    def apply[A](op: CreateDraftLogic[A]): BufferState[A] =
      val result = try {
        interpreter(op.asInstanceOf[CreateDraftLogic[Any]])
      } catch {
        case NonFatal(e) => sys.error(s"error casting $op: $e")
      }

      try {
        result.asInstanceOf[BufferState[A]]
      } catch {
        case NonFatal(e) => sys.error(s"$result wasn't A: $e")
      }

  def exampleContract = DraftContract(
    CounterpartyA("counterparty-A"),
    CounterpartyB("counterparty-B"),
    "the terms of the contract",
    "the conditions of the contract"
  )

  def apply(draft : DraftContract = exampleContract) = {
    val logic: Free[CreateDraftLogic, CreateDraftResponse] = CreateDraftLogic(draft)

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[BufferState]

    // .. and then "run" that state by giving it an empty buffer
    val buffer = run(Buffer(Nil))._2

    println(buffer)
  }
}
