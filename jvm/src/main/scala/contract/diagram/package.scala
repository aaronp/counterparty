package contract

package object diagram {

  enum Participants:
    case ContractService
    case Database
    case CounterpartyA
    case CounterpartyB

  type MermaidInstruction = String
  extension (calls: List[MermaidInstruction])
    // note: the 'reverse' is because we'll ultimately have to reverse the whole stack as we're prepending instructions
    def asState[A](result: A): State[Calls, A] = State.combine[Calls, A](calls.reverse, result)
  extension (call: MermaidInstruction)
    def asState[A](result: A): State[Calls, A] = List(call).asState(result)

  type Calls = List[MermaidInstruction]
  extension (calls: Calls)
    def asMermaidDiagram(participants: Seq[String]): String = {
      (participants.map(p => s"participant $p") ++ calls)
        .mkString("sequenceDiagram\n\t", "\n\t", "\n")
    }

  type CallsState[A] = State[Calls, A]
}
