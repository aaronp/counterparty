package contract.server

import contract.*
import zio.{Fiber, Task, ZIO}

import scala.annotation.targetName

class SignContractInMemory() extends EnactContract {

  import EnactContractLogic.*

  override type Async[Result] = Fiber.Runtime[Throwable,Result]


  // calls the method under test -- namely our CreateDraftLogic
  def run(request: SignContract): Task[SignContractResponse] = EnactContractLogic(request).foldMap[Task]

  private var sentContractsA : List[CounterpartyRef] = Nil
  private var sentContractsB : List[CounterpartyRef] = Nil

  def signedContractsForA = sentContractsA
  def signedContractsForB = sentContractsB


  // our fake business logic
  private def sign(isCounterpartyA : Boolean, ref : CounterpartyRef) : CounterpartyRef | NotSignedReason= {
    if isCounterpartyA then
      sentContractsA = ref +: sentContractsA
    else
      sentContractsB = ref +: sentContractsB
    if ref.toString.startsWith("!") then NotSignedReason(s"didn't sign $ref") else ref
  }

  /* This given (implicit) allows us to call 'foldLeft' on our logic
   */
  @targetName("enactContractLogicAsTask")
  given~>[EnactContractLogic, Task] with
    def apply[A](msg: EnactContractLogic[A]): Task[A] = msg match {
      case RequestSignatureA(ref) => ZIO.attempt(sign(true, ref)).fork
      case RequestSignatureB(ref) => ZIO.attempt(sign(false, ref)).fork
      case Wait(fiber) => fiber.join
      case LogMessage(msg) => zio.Console.printLine(msg)
    }

}
