package contract.server

import contract.{Contract, CounterpartyRef, CreateDraftLogic, DraftContract, DraftContractId}
import contract.CreateDraftLogic.{
  LogMessage,
  NotifyCounterpartyA,
  NotifyCounterpartyB,
  StoreDraftInDatabase
}

import counterparty.service.server.model.CreateDraftResponse
import contract.{Contract, CounterpartyRef, CreateDraftLogic, DraftContract, DraftContractId}
import contract.CreateDraftLogic.*
import contract.~>
import zio.*

import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.HashMap

trait CreateDraftHandler {
  def storeDraftInDatabase(contract: DraftContract): DraftContractId

  def notifyCounterpartyA(contract: Contract): CounterpartyRef

  def notifyCounterpartyB(contract: Contract): CounterpartyRef

  def log(msg: String): Unit

  def onMessage[A](msg: CreateDraftLogic[A]) = msg match {
    case StoreDraftInDatabase(draft)   => storeDraftInDatabase(draft)
    case NotifyCounterpartyA(contract) => notifyCounterpartyA(contract)
    case NotifyCounterpartyB(contract) => notifyCounterpartyB(contract)
    case LogMessage(msg) =>
      log(msg)
      () // <--- we need this here, as the return type from our log operation is Unit
  }

  // handles the draft contract
  def run(draft: DraftContract): Task[CreateDraftResponse] = CreateDraftLogic(draft).foldMap[Task]

  /* This given (implicit) allows us to call 'foldLeft' on our logic
   */
  @targetName("createDraftLogicAsTask")
  given ~>[CreateDraftLogic, Task] with
    def apply[A](op: CreateDraftLogic[A]): Task[A] = ZIO.attempt(onMessage(op).asInstanceOf[A])

}

object CreateDraftHandler {

  // a noddy / fake database
  class FakeDatabase(
      val contracts: mutable.Map[DraftContractId, DraftContract] =
        HashMap[DraftContractId, DraftContract]()
  ) {
    def save(draft: DraftContract): DraftContractId = {
      val id = DraftContractId(s"id-${contracts.size}")
      contracts += id -> draft
      id
    }
  }

  class FakeCounterpartyService(
      val contracts: mutable.ListBuffer[Contract] = mutable.ListBuffer()
  ) {
    def notify(contract: Contract) = {
      contracts += contract
      CounterpartyRef(s"Ack-${contract.id}")
    }
  }

  /** The 'TestEnv' provides fake, in-memory implementations for our operations
    */
  class InMemory(
      counterPartyA: FakeCounterpartyService = FakeCounterpartyService(),
      counterPartyB: FakeCounterpartyService = FakeCounterpartyService(),
      db: FakeDatabase = FakeDatabase(),
      logs: mutable.ListBuffer[String] = mutable.ListBuffer()
  ) extends CreateDraftHandler {

    def asMap = Map(
      "counterpartyA" -> counterPartyA.contracts.toList,
      "counterpartyB" -> counterPartyB.contracts.toList,
      "db"            -> db.contracts.toMap,
      "logs"          -> logs.toList
    )

    override def storeDraftInDatabase(contract: DraftContract) = db.save(contract)

    override def notifyCounterpartyA(contract: Contract) = counterPartyA.notify(contract)

    override def notifyCounterpartyB(contract: Contract) = counterPartyB.notify(contract)

    override def log(msg: String): Unit = logs += msg
  }
}
