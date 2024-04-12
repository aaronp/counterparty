package free.contract

import CreateDraftLogic.*
import _root_.free.contract.{*, given}
import free.*

import scala.annotation.targetName

@main def generateMermaidDiagram(): Unit = {
  val contract = DraftContract.testData
  val sequenceDiagram = Mermaid(contract)
  val script = s"""cat > diagram.md << 'EOF'
                  |# Sequence Diagram
                  |```mermaid
                  |$sequenceDiagram
                  |```
                  |EOF
                  |
                  |docker run --rm -v "$$PWD:/data" minlag/mermaid-cli -i /data/diagram.md -o /data/diagram.svg
                  |""".stripMargin
  println(script)
}

/**
 * Turn an operation into a mermaid diagram
 */
object Mermaid {

  object participants {
    val Svc = "ContractService"
    val DB = "Database"
    val CtrPtyA = "CounterpartyA"
    val CtrPtyB = "CounterpartyB"
    def all = List(Svc, DB, CtrPtyA, CtrPtyB)
  }
  type MermaidInstruction = String

  extension (calls: List[MermaidInstruction])
    // note: the 'reverse' is because we'll ultimately have to reverse the whole stack as we're prepending instructions
    def asState[A](result: A): State[Calls, A] = State.combine[Calls, A](calls.reverse, result)
  extension (call: MermaidInstruction)
    def asState[A](result: A): State[Calls, A] = List(call).asState(result)

  type Calls = List[MermaidInstruction]
  extension (calls: Calls)
    def asMermaidDiagram: String = {
      (participants.all.map(p => s"participant $p") ++ calls).mkString("sequenceDiagram\n\t","\n\t","\n")
    }


  /**
   * An interpreter which turns our commands into mermaid syntax (built up in a State monad)
   */
  import participants.*
  def mermaidInterpreter[A]: CreateDraftLogic[A] => State[Calls, A] = (_: CreateDraftLogic[A]) match {
    case StoreDraftInDatabase(draft) =>
      val result = DraftContractId.create()
      List(
        s"$Svc->>$DB: save draft",
        s"$DB-->>$Svc: returning $result"
      ).asState(result)
    case NotifyCounterpartyA(contract) =>
      val result = CounterpartyRef(s"notified party A of ${contract.id}")
      List(
        s"$Svc->>$CtrPtyA : notify of ${contract.id}",
        s"$CtrPtyA-->>$Svc : $result"
      ).asState(result)
    case NotifyCounterpartyB(contract) =>
      val result = CounterpartyRef(s"notified party B of ${contract.id}")
      List(
        s"$Svc->>$CtrPtyB : notify of ${contract.id}",
        s"$CtrPtyB-->>$Svc : $result"
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
