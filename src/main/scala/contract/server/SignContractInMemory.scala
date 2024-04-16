package contract.server

import contract.*
import zio.{Fiber, Task, ZIO}

import scala.annotation.targetName

trait SignContractHandler extends EnactContract {

  override type Async[Result] = Fiber.Runtime[Throwable, Result]

  import EnactContractLogic.*

  def requestSignatureA(counterpartyRef: CounterpartyRef): CounterpartyRef | NotSignedReason

  def requestSignatureB(counterpartyRef: CounterpartyRef): CounterpartyRef | NotSignedReason

  def log(msg: String) = zio.Console.printLine(msg)

  def onMessage[A](msg: EnactContractLogic[A]): ZIO[Any, Throwable, Any] = msg match {
    case RequestSignatureA(ref) => ZIO.attempt(requestSignatureA(ref)).fork
    case RequestSignatureB(ref) => ZIO.attempt(requestSignatureB(ref)).fork
    case Wait(fiber) => fiber.join
    case LogMessage(msg) => log(msg)
  }

  // calls the method under test -- namely our CreateDraftLogic
  def run(request: SignContract): Task[SignContractResponse] = EnactContractLogic(request).foldMap[Task]

  /* This given (implicit) allows us to call 'foldLeft' on our logic
   */
  @targetName("enactContractLogicAsTask")
  given~>[EnactContractLogic, Task] with
    def apply[A](msg: EnactContractLogic[A]): Task[A] = onMessage(msg).asInstanceOf[Task[A]]

}

object SignContractHandler {

  class InMemory() extends SignContractHandler {

    private var sentContractsA: List[CounterpartyRef] = Nil
    private var sentContractsB: List[CounterpartyRef] = Nil

    def signedContractsForA = sentContractsA

    def signedContractsForB = sentContractsB

    // our fake business logic
    private def sign(isCounterpartyA: Boolean, ref: CounterpartyRef): CounterpartyRef | NotSignedReason = {
      if isCounterpartyA then
        sentContractsA = ref +: sentContractsA
      else
        sentContractsB = ref +: sentContractsB
      if ref.toString.startsWith("!") then NotSignedReason(s"didn't sign $ref") else ref
    }

    override def requestSignatureA(counterpartyRef: CounterpartyRef) = sign(true, counterpartyRef)

    override def requestSignatureB(counterpartyRef: CounterpartyRef) = sign(false, counterpartyRef)
  }
}
