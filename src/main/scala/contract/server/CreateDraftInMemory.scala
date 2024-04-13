package contract.server

import counterparty.service.server.model.CreateDraftResponse
import contract.{Contract, CounterpartyRef, CreateDraftLogic, DraftContract, DraftContractId}
import contract.CreateDraftLogic.*
import contract.~>
import zio.*

import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.HashMap


/**
 * The 'TestEnv' provides fake, in-memory implementations for our operations
 */
class InMemoryEnv(counterPartyA: FakeCounterpartyService = FakeCounterpartyService(),
                  counterPartyB: FakeCounterpartyService = FakeCounterpartyService(),
                  db: FakeDatabase = FakeDatabase(),
                  logs: mutable.ListBuffer[String] = mutable.ListBuffer()
             ) {

  /* This given (implicit) allows us to call 'foldLeft' on our logic
   */
  @targetName("createDraftLogicAsTask")
  given~>[CreateDraftLogic, Task] with
    def apply[A](op: CreateDraftLogic[A]): Task[A] = ZIO.attempt(onMessage(op).asInstanceOf[A])

  def asMap = Map(
    "counterpartyA" -> counterPartyA.contracts.toList,
    "counterpartyB" -> counterPartyB.contracts.toList,
    "db" -> db.contracts.toMap,
    "logs" -> logs.toList
  )

  // calls the method under test -- namely our CreateDraftLogic
  def run(draft: DraftContract): Task[CreateDraftResponse] = CreateDraftLogic(draft).foldMap[Task]

  /**
   * NOTE - if we want to 'mock' (or track, or whatever) any of our operations, it would look like a one-line pattern
   * match as per here:
   * @param msg
   * @tparam A
   * @return the operation result
   */
  def onMessage[A](msg: CreateDraftLogic[A]) = msg match {
    case StoreDraftInDatabase(draft) => db.save(draft)
    case NotifyCounterpartyA(contract) => counterPartyA.notify(contract)
    case NotifyCounterpartyB(contract) => counterPartyB.notify(contract)
    case LogMessage(msg) =>
      logs += msg
      () // <--- we need this here, as the return type from our log operation is Unit
  }

}
// a noddy / fake database
class FakeDatabase(val contracts: mutable.Map[DraftContractId, DraftContract] = HashMap[DraftContractId, DraftContract]()) {
  def save(draft: DraftContract): DraftContractId = {
    val id = DraftContractId(s"id-${contracts.size}")
    contracts += id -> draft
    id
  }
}

class FakeCounterpartyService(val contracts: mutable.ListBuffer[Contract] = mutable.ListBuffer()) {
  def notify(contract: Contract) = {
    contracts += contract
    CounterpartyRef(s"Ack-${contract.id}")
  }
}
