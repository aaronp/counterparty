package contract.js.interactive

import contract.{CreateDraftLogic, DraftContract}
import support.CompletedCall
import support.State
import support.*

object SvgForCalls {

  def apply(calls: Seq[CompletedCall]): Seq[SendMessage] = {
    SendMessage.fromCalls(calls.sortBy(_.timestamp))
  }

}
