package support

import scala.concurrent.duration.Duration
import scala.concurrent.duration.{given, *}
import _root_.support.Telemetry.*

/** Representation of a message being sent from one actor to another
  */
case class SendMessage(
    from: Actor,
    to: Actor,
    timestamp: Long,
    duration: Duration,
    message: ujson.Value
) {
  def endTimestamp = timestamp + duration.toMillis

  def isActiveAt(time: Long) = timestamp <= time && time <= timestamp + duration.toMillis
}

object SendMessage {
  def test = SendMessage(
    support.Actor.person("foo", "dave"),
    support.Actor.person("bar", "carl"),
    1234,
    10.seconds,
    ujson.Obj("hello" -> "world")
  )

  /** This recursive function walks through the calls, keeping track of which participants are
    * currently active.
    *
    * If a call starts and then completes before the next call (i.e. synchronous invocation), we
    * don't bother activating a participant, and we don't add to the sortedCompleted, we just add a
    * "source -->> target" line
    *
    * If the next call DOES come before the current call's end timestamp, then we add a "source
    * -->>+ target" line and add the call to the sortedCompleted (which stays sorted) We then add a
    * target -->>- source line for the return call which deactivates the target participant
    *
    * @param sortedCalls
    *   the full sorted call stack we're walking through
    * @param sortedCompleted
    *   our buffer of async calls which have completed after the next call
    * @param buffer
    *   the buffer of statements we're appending to
    * @return
    *   a sequence of mermaid statements
    */
  def fromCalls(
      sortedCalls: Seq[CompletedCall],
      sortedCompleted: Seq[CompletedCall] = Vector(),
      buffer: Seq[SendMessage] = Vector()
  ): Seq[SendMessage] = {

    def truncate(owt: Any, len: Int = 85) =
      val opString = owt.toString
      if opString.length > len then opString.take(len - 3) + "..." else opString

    def selfCall(call: CompletedCall): SendMessage = {

      // s"${call.source} ->> ${call.target} : ${truncate(call.operation)} ${truncate(commentForResult(call), 30)}"
      ???
    }

    def startCall(call: CompletedCall, arrow: String): SendMessage = {
      // s"${call.source} $arrow ${call.target} : ${truncate(call.operation)}"
      ???
    }

    def commentForResult(call: CompletedCall) = call.response match {
      case CallResponse.NotCompleted         => "never completed"
      case CallResponse.Error(_, error)      => s"Errored with '$error'"
      case CallResponse.Completed(_, result) => s"Returned '$result'"
    }

    def endCall(call: CompletedCall, arrow: String): Option[SendMessage] = call.response match {
      case CallResponse.NotCompleted    => None
      case CallResponse.Error(_, error) =>
        // Some(s"${call.target} $arrow ${call.source} : ${commentForResult(call)}")
        ???
      case CallResponse.Completed(_, result) =>
        // Some(s"${call.target} $arrow ${call.source} : ${commentForResult(call)}")
        ???
    }

    // NOTE - this is technically inefficient, but we're talking about lists of 2 or 3 items
    def appendInProgress(call: CompletedCall) =
      (call +: sortedCompleted).sortBy(_.endTimestamp.getOrElse(-1L))

    (sortedCalls, sortedCompleted) match {
      case (Seq(), Seq()) => buffer
      case (Seq(), nextCompleted +: theRestCompleted) =>
        fromCalls(
          Seq(),
          theRestCompleted,
          buffer :++ endCall(nextCompleted, "-->>-")
        )
      case (Seq(nextCall), Seq()) =>
        buffer :+ startCall(nextCall, "->>")
      // the case when our next completion ends before or at the same time as the next call:
      case (nextCall +: theRestCalls, inProgress +: theRestInProgress)
          if inProgress.endTimestamp.exists(_ <= nextCall.timestamp) =>
        fromCalls(
          nextCall +: theRestCalls,
          theRestInProgress,
          buffer :++ endCall(inProgress, "-->>-")
        )
      // the case when our next call doesn't complete until after the subsequent call:
      case (callA +: callB +: theRestCalls, _) if callA.endTimestamp.exists(_ > callB.timestamp) =>
        fromCalls(
          callB +: theRestCalls,
          appendInProgress(callA),
          buffer :+ startCall(callA, "->>+")
        )
      // the typical synchronous call case
      case (callA +: theRestCalls, theRestInProgress) =>
        val calls = if callA.source == callA.target then {
          selfCall(callA) :: Nil
        } else {
          startCall(callA, "->>") +: endCall(callA, "-->>").toSeq
        }
        fromCalls(
          theRestCalls,
          theRestInProgress,
          buffer :++ calls
        )
    }
  }
}
