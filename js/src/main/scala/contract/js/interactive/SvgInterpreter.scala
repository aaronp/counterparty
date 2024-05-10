package contract.js.interactive

import contract.*
import scala.annotation.targetName
import CreateDraftLogic.*
import scala.concurrent.duration.{given, *}
import upickle.default.*
import contract.model.CreateDraftResponse
import support.*
import support.{~>, State}

object SvgInterpreter {

  val actors: List[Actor] = List(
    Actor.service(Category.ContractSystem.toString(), "Contract Service"),
    Actor.database(Category.ContractSystem.toString(), "MongoDB"),
    Actor.queue(Category.ContractSystem.toString(), "Notification Queue"),
    //
    Actor.job(Category.CounterpartyA.toString(), "Counterparty Adapter"),
    Actor.email(Category.CounterpartyA.toString(), "Email Server"),
    Actor.person(Category.CounterpartyA.toString(), "Bob"),
    Actor.person(Category.CounterpartyA.toString(), "Lawyer"),
    //
    Actor.job(Category.CounterpartyB.toString(), "Counterparty Adapter"),
    Actor.service(Category.CounterpartyB.toString(), "CRM Adapter"),
    Actor.person(Category.CounterpartyB.toString(), "Susie")
  )

  val List(contractService, mongo, queue, adapterA, email, bob, lawyer, adapterB, crm, susie) =
    actors

  // we need a natural transformation to convert our operation types
  // into a 'BufferState' -- which is something that just keeps a
  // list of the operations called and their responses
  @targetName("createDraftLogicAsSvg")
  given ~>[CreateDraftLogic, FoldState] with
    def apply[A](op: CreateDraftLogic[A]): State[FoldData, A] = {
      var clock = 0L
      def inc(offset: Int = 10) = {
        val now = clock
        clock += offset
        clock
      }

      op match {
        case StoreDraftInDatabase(draft) =>
          val result = DraftContractId(s"contract for ${draft.terms}")
          FoldData(
            SendMessage(
              contractService,
              mongo,
              inc(),
              150.millis,
              "-->>",
              writeJs(draft.asData)
            )
          ).asState(result)
        case NotifyCounterpartyA(contract) =>
          val result = CounterpartyRef(s"notified party A of ${contract.id}")
          val now    = inc()
          FoldData(
            SendMessage(
              contractService,
              queue,
              now,
              250.millis,
              "-->>",
              ujson.Obj(
                "to"       -> "partyA",
                "ref"      -> contract.id.toString(),
                "contract" -> writeJs(contract.draft.asData)
              )
            ),
            SendMessage(
              queue,
              adapterA,
              now + 500,
              50.millis,
              "-->>",
              ujson.Obj("emailBody" -> "dear")
            ),
            SendMessage(
              adapterA,
              email,
              now + 600,
              800.millis,
              "-->>",
              ujson.Obj("emailBody" -> "dear sir or madam...")
            ),
            SendMessage(
              adapterA,
              contractService,
              now + 600,
              500.millis,
              "-->>",
              ujson.Obj("ref" -> result.toString())
            ),
            SendMessage(
              email,
              bob,
              now + 1100,
              500.millis,
              "-->>",
              ujson.Obj("emailBody" -> "dear sir or madam...")
            )
          ).asState(result)
        case NotifyCounterpartyB(contract) =>
          val result = CounterpartyRef(s"notified party A of ${contract.id}")
          val now    = inc()
          FoldData(
            SendMessage(
              contractService,
              queue,
              now,
              250.millis,
              "-->>",
              ujson.Obj(
                "to"       -> "partyA",
                "ref"      -> contract.id.toString(),
                "contract" -> writeJs(contract.draft.asData)
              )
            ),
            SendMessage(
              queue,
              adapterB,
              now + 700,
              150.millis,
              "-->>",
              ujson.Obj("record" -> "CRM entry...")
            ),
            SendMessage(
              adapterB,
              crm,
              now + 800,
              450.millis,
              "-->>",
              ujson.Obj("emailBody" -> "dear sir or madam...")
            ),
            SendMessage(
              adapterB,
              contractService,
              now + 850,
              500.millis,
              "-->>",
              ujson.Obj("ref" -> result.toString())
            ),
            SendMessage(
              crm,
              susie,
              now + 1800,
              500.millis,
              "-->>",
              ujson.Obj("open" -> "reads CRM report...")
            )
          ).asState(result)
        case LogMessage(msg) => FoldData(Nil).asState(())
      }
    }

  /** function which turns the request into a set of messages and actors
    * @param draft
    *   the request
    */
  def messages(draft: DraftContract = DraftContract.testData): Seq[SendMessage] = {
    val logic = CreateDraftLogic(draft)

    // convert our 'Free' (logic) into a buffer (e.g. tree of operations)
    val State(run) = logic.foldMap[FoldState]

    val (_, foldData) = run(FoldData(Nil))
    foldData.messages.sortBy(_.timestamp)
  }
}
