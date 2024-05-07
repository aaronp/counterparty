package contract

import contract.*
import zio.*

import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag

/** Telemetry is a trait which allows us to track the calls made in our system, which we can later
  * use to show what happened
  *
  * @param callsStackRef
  */
trait Telemetry(val callsStackRef: Ref[CallStack]) {

  /** @return
    *   a operation which will access the trace calls and render them as a mermaid block
    */
  def asMermaidDiagram(
      mermaidStyle: String = """%%{init: {"theme": "dark", 
"themeVariables": {"primaryTextColor": "grey", "secondaryTextColor": "black", "fontFamily": "Arial", "fontSize": 14, "primaryColor": "#3498db"}}}%%"""
  ): UIO[String] =
    asMermaidSequenceDiagram.map(sd => s"\n```mermaid\n$mermaidStyle\n${sd}```\n")

  /** This is just the sequence block part of the mermaid diagram. See 'asMermaidDiagram' for the
    * full markdown version
    *
    * @return
    *   the calls as a mermaid sequence diagram
    */
  def asMermaidSequenceDiagram: UIO[String] = calls.map { all =>
    val statements = Telemetry.asMermaidStatements(all.sortBy(_.timestamp))

    // we want to group the participants together by category
    // this
    val participants = {
      val (orderedCategories, actorsByCategory) = all
        .sortBy(_.timestamp)
        .foldLeft((Seq[String](), Map[String, Seq[Actor]]())) {
          case ((categories, coordsByCategory), call) =>
            val newMap = coordsByCategory
              .updatedWith(call.source.category) {
                case None                                => Some(Seq(call.source))
                case Some(v) if !v.contains(call.source) => Some(v :+ call.source)
                case values                              => values
              }
              .updatedWith(call.target.category) {
                case None                                => Some(Seq(call.target))
                case Some(v) if !v.contains(call.target) => Some(v :+ call.target)
                case values                              => values
              }
            val newCategories = {
              val srcCat =
                if categories.contains(call.source.category) then categories
                else categories :+ call.source.category

              if srcCat.contains(call.target.category) then srcCat
              else srcCat :+ call.target.category
            }
            (newCategories, newMap)
        }

      orderedCategories
        .zip(support.Colors.namedColors.take(orderedCategories.size))
        .flatMap { (category, color) =>

          val participants = actorsByCategory
            .getOrElse(category, Nil)
            .map(a => s"participant $a")
          List(s"box $color $category") ++ participants ++ List("end")
        }
    }

    (participants ++ statements).mkString("sequenceDiagram\n\t", "\n\t", "\n")
  }

  def calls: UIO[Seq[CompletedCall]] = {
    for
      callStack <- callsStackRef.get
      calls     <- ZIO.foreach(callStack.calls)(_.asCompletedCall)
    yield calls
  }

  // def onCall[F[_], A](source: Actor, target: Actor, operation: F[A]): ZIO[Any, Nothing, Call] = {
  def onCall[F[_], A](source: Actor, target: Actor, input: Any): ZIO[Any, Nothing, Call] = {
    for
      call <- Call(source, target, input)
      _    <- callsStackRef.update(_.add(call))
    yield call
  }
}

object Telemetry {
  def apply(): Telemetry = make().execOrThrow()
  def make() = {
    for calls <- Ref.make(CallStack())
    yield new Telemetry(calls) {}
  }

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
    * @param mermaidBuffer
    *   the buffer of statements we're appending to
    * @return
    *   a sequence of mermaid statements
    */
  private def asMermaidStatements(
      sortedCalls: Seq[CompletedCall],
      sortedCompleted: Seq[CompletedCall] = Vector(),
      mermaidBuffer: Seq[String] = Vector()
  ): Seq[String] = {

    def truncate(owt: Any, len: Int = 15) =
      val opString = owt.toString
      if opString.length > len then opString.take(len - 3) + "..." else opString

    def selfCall(call: CompletedCall) =
      s"${call.source} ->> ${call.target} : ${truncate(call.operation)} ${truncate(commentForResult(call), 30)}"

    def startCall(call: CompletedCall, arrow: String) =
      s"${call.source} $arrow ${call.target} : ${truncate(call.operation)}"

    def commentForResult(call: CompletedCall) = call.response match {
      case CallResponse.NotCompleted         => "never completed"
      case CallResponse.Error(_, error)      => s"Errored with '$error'"
      case CallResponse.Completed(_, result) => s"Returned '$result'"
    }

    def endCall(call: CompletedCall, arrow: String) = call.response match {
      case CallResponse.NotCompleted => None
      case CallResponse.Error(_, error) =>
        Some(s"${call.target} $arrow ${call.source} : ${commentForResult(call)}")
      case CallResponse.Completed(_, result) =>
        Some(s"${call.target} $arrow ${call.source} : ${commentForResult(call)}")
    }

    // NOTE - this is technically inefficient, but we're talking about lists of 2 or 3 items
    def appendInProgress(call: CompletedCall) =
      (call +: sortedCompleted).sortBy(_.endTimestamp.getOrElse(-1L))

    (sortedCalls, sortedCompleted) match {
      case (Seq(), Seq()) => mermaidBuffer
      case (Seq(), nextCompleted +: theRestCompleted) =>
        asMermaidStatements(
          Seq(),
          theRestCompleted,
          mermaidBuffer :++ endCall(nextCompleted, "-->>-")
        )
      case (Seq(nextCall), Seq()) =>
        mermaidBuffer :+ startCall(nextCall, "->>")
      // the case when our next completion ends before or at the same time as the next call:
      case (nextCall +: theRestCalls, inProgress +: theRestInProgress)
          if inProgress.endTimestamp.exists(_ <= nextCall.timestamp) =>
        asMermaidStatements(
          nextCall +: theRestCalls,
          theRestInProgress,
          mermaidBuffer :++ endCall(inProgress, "-->>-")
        )
      // the case when our next call doesn't complete until after the subsequent call:
      case (callA +: callB +: theRestCalls, _) if callA.endTimestamp.exists(_ > callB.timestamp) =>
        asMermaidStatements(
          callB +: theRestCalls,
          appendInProgress(callA),
          mermaidBuffer :+ startCall(callA, "->>+")
        )
      // the typical synchronous call case
      case (callA +: theRestCalls, theRestInProgress) =>
        val calls = if callA.source == callA.target then {
          selfCall(callA) :: Nil
        } else {
          startCall(callA, "->>") +: endCall(callA, "-->>").toSeq
        }
        asMermaidStatements(
          theRestCalls,
          theRestInProgress,
          mermaidBuffer :++ calls
        )
    }
  }
}

def now = Clock.currentTime(TimeUnit.NANOSECONDS)

case class CallStack(calls: Seq[Call] = Vector()) {
  def add(call: Call) = copy(calls :+ call)
}

enum CallResponse:
  case NotCompleted
  case Error(timestamp: Long, bang: Any)
  case Completed(timestamp: Long, result: Any)
  def timestampOpt: Option[Long] = this match {
    case NotCompleted     => None
    case Error(ts, _)     => Option(ts)
    case Completed(ts, _) => Option(ts)
  }

final case class CallSite(
    source: Actor,
    target: Actor,
    operation: Any,
    timestamp: Long
)
final case class CompletedCall(invocation: CallSite, response: CallResponse) {
  import ConsoleColors.*
  export invocation.*
  def atDateTime = java.time.Instant.ofEpochMilli(timestamp)

  def endTimestamp = response.timestampOpt.ensuring {
    case None      => true
    case Some(end) => end >= timestamp
  }

  private def fmtDuration(end: Long, start: Long) = {
    val millis = (end.toDouble - start.toDouble) / 1000000
    f"$millis%.4fms"
  }
  def duration = response match {
    case CallResponse.Error(end, _)     => fmtDuration(end, timestamp)
    case CallResponse.Completed(end, _) => fmtDuration(end, timestamp)
    case CallResponse.NotCompleted      => "♾️"
  }

  def resultText = response match {
    case CallResponse.Error(_, err)        => ConsoleColors.red(s"failed with $err")
    case CallResponse.Completed(_, result) => ConsoleColors.green(s"returned $result")
    case CallResponse.NotCompleted         => ConsoleColors.yellow("never completed")
  }
  override def toString = toStringColor

  def toStringMonocolor =
    s"$source --[ $operation ]--> $target $resultText and took $duration at $atDateTime"

  def operationArrow = s"--[ $operation ]-->"

  def toStringColor =
    s"$timestamp: ${blue(source.toString)} ${purple(operationArrow)} ${yellow(target.toString)} $resultText and took $duration at $atDateTime"
}

/** Represents a Call invocation -- something we'd want to capture in our archtiecture (i.e. a
  * sequence diagram describing our system)
  *
  * @param source
  * @param target
  * @param operation
  * @param input
  *   the input which triggered this call
  * @param timestamp
  *   the time the call was made
  */
final class Call(
    invocation: CallSite,
    response: Ref[CallResponse]
) {

  def asCompletedCall: UIO[CompletedCall] = {
    response.get.map { resp =>
      CompletedCall(invocation, resp)
    }
  }

  /** completeWith will always finish the calls, even on error
    *
    * @param result
    * @return
    *   a 'pimped' Task which will update the response ref when run
    */
  def completeWith[A](result: Task[A]): Task[A] = {
    for
      either <- result.either
      time   <- now
      result <- either match {
        case Left(err) =>
          for
            _      <- response.set(CallResponse.Error(time, err))
            failed <- ZIO.fail(err)
          yield failed
        case Right(ok) =>
          response.set(CallResponse.Completed(time, ok)).as(ok)
      }
    yield result
  }
}

object Call {
  def apply(source: Actor, target: Actor, operation: Any): ZIO[Any, Nothing, Call] = {
    for
      time        <- now
      responseRef <- Ref.make[CallResponse](CallResponse.NotCompleted)
    yield new Call(CallSite(source, target, operation, time), responseRef)
  }
}
