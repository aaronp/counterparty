package free.examples.microservice

import _root_.free.examples.microservice.CreateDraftLogic.*
import _root_.free.examples.{*, given}
import free.*

import scala.annotation.targetName

@main def generateMermaidDiagram(): Unit = {
  val contract = DraftContract.testData
  val sequenceDiagram = Mermaid(contract)
  val script = asDockerScript(sequenceDiagram)
  
  val path = java.nio.file.Paths.get("createDiagram.sh") 
  java.nio.file.Files.writeString(path, script)
}


def asDockerScript(diagram: String) = {
  s"""cat << EOF > diagram.md
     |# Sequence Diagram
     |```mermaid
     |$diagram
     |```
     |EOF
     |
     |docker run --rm -v "$$PWD:/data" minlag/mermaid-cli -i /data/diagram.md -o /data/output.svg
     |""".stripMargin
}
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
  extension (calls: List[Call])
    def asState[A](result: A): State[Calls, A] = State.combine[Calls, A](calls, result)
  extension (call: Call)
    def asState[A](result: A): State[Calls, A] = List(call).asState(result)

  type Calls = List[Call]
  extension (calls: Calls)
    def asMermaidDiagram: String = {
      (actors.all.map(p => s"participant $p") ++ calls).mkString("sequenceDiagram\n\t","\n\t","\n")
    }

  import actors.*
  def mermaidInterpreter[A]: CreateDraftLogic[A] => State[Calls, A] = (_: CreateDraftLogic[A]) match {
    case StoreDraftInDatabase(draft) =>
      val result = DraftContractId.create()
      List(
        s"$Svc ->> $DB: save draft",
        s"$DB --> $DB: $result"
      ).asState(result)
    case NotifyCounterpartyA(contract) =>
      val result = CounterpartyRef(s"notified party A of ${contract.id}")
      List(
        s"$Svc ->> $CtrPtyA : notify of ${contract.id}",
        s"$CtrPtyA --> $Svc : $result"
      ).asState(result)
    case NotifyCounterpartyB(contract) =>
      val result = CounterpartyRef(s"notified party B of ${contract.id}")
      List(
        s"$Svc ->> $CtrPtyB : notify of ${contract.id}",
        s"$CtrPtyB --> $Svc : $result"
      ).asState(result)
    case LogMessage(msg) => s"Note right of $Svc : $msg".asState(())
  }

  type CallsState[A] = State[Calls, A]

  // we need a natural transformation to convert our operation types
  // into a 'BufferState' -- which is something that just keeps a
  // list of the operations called and their responses
  @targetName("createDraftLogicAsBufferState")
  given~>[CreateDraftLogic, CallsState] with
    def apply[A](op: CreateDraftLogic[A]): State[Calls, A] = mermaidInterpreter(op)

  def apply(draft: DraftContract): String = {
    val logic = CreateDraftLogic(draft)

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[CallsState]

    // .. and then "run" that state by giving it an empty buffer
    run(Nil)._2.reverse.asMermaidDiagram
  }
}