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

  def calls: UIO[Seq[CompletedCall]] = {
    for {
      callStack <- callsStackRef.get
      calls     <- ZIO.foreach(callStack.calls)(_.asCompletedCall)
    } yield calls
  }

  // def onCall[F[_], A](source: Coords, target: Coords, operation: F[A]): ZIO[Any, Nothing, Call] = {
  def onCall[F[_], A](source: Coords, target: Coords, input: Any): ZIO[Any, Nothing, Call] = {
    for {
      call <- Call(source, target, input)
      _    <- callsStackRef.update(_.add(call))
    } yield call
  }
}

object Telemetry {
  def apply(): Telemetry = make().execOrThrow()
  def make() = {
    for {
      calls <- Ref.make(CallStack())
    } yield new Telemetry(calls) {}
  }
}

def now = Clock.currentTime(TimeUnit.NANOSECONDS)

case class CallStack(calls: Seq[Call] = Vector()) {
  def add(call: Call) = copy(calls :+ call)
}

/** @param namespace
  *   the namespace of the app - this is needed to group our systems
  * @param app
  *   the name of the system - what or who is invoking the call
  */
final case class Coords(namespace: String, app: String) {
  override def toString = s"$namespace.$app"
}
object Coords {
  def default(app: String): Coords       = Coords("default", app)
  def apply[A: ClassTag](svc: A): Coords = apply[A]

  def apply[A: ClassTag]: Coords = {
    val fullyQualifiedName = summon[ClassTag[A]].runtimeClass.getName
    val parts              = fullyQualifiedName.split("\\.", -1).toSeq
    val packageName        = parts.init.last
    val name               = parts.last.takeWhile(_ != '$')

    new Coords(packageName, name)
  }
}

enum CallResponse:
  case NotCompleted
  case Error(timestamp: Long, bang: Any)
  case Completed(timestamp: Long, result: Any)

final case class CallSite(
    source: Coords,
    target: Coords,
    operation: Any,
    timestamp: Long
)
object ConsoleColors {
  // ANSI escape codes for colors
  private val RESET  = "\u001B[0m"
  private val RED    = "\u001B[31m"
  private val GREEN  = "\u001B[32m"
  private val YELLOW = "\u001B[33m"
  private val BLUE   = "\u001B[34m"
  private val PURPLE = "\u001B[35m"
  private val CYAN   = "\u001B[36m"
  private val WHITE  = "\u001B[37m"

  // Colored output methods
  def red(text: String): String    = s"$RED$text$RESET"
  def green(text: String): String  = s"$GREEN$text$RESET"
  def yellow(text: String): String = s"$YELLOW$text$RESET"
  def blue(text: String): String   = s"$BLUE$text$RESET"
  def purple(text: String): String = s"$PURPLE$text$RESET"
  def cyan(text: String): String   = s"$CYAN$text$RESET"
  def white(text: String): String  = s"$WHITE$text$RESET"
}
final case class CompletedCall(invocation: CallSite, response: CallResponse) {
  import ConsoleColors.*
  export invocation.*
  def atDateTime = java.time.Instant.ofEpochMilli(timestamp)

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
    s"${blue(source.toString)} ${purple(operationArrow)} ${yellow(target.toString)} $resultText and took $duration at $atDateTime"
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
    for {
      either <- result.either
      time   <- now
      result <- either match {
        case Left(err) =>
          for {
            _      <- response.set(CallResponse.Error(time, err))
            failed <- ZIO.fail(err)
          } yield failed
        case Right(ok) =>
          response.set(CallResponse.Completed(time, ok)).as(ok)
      }
    } yield result
  }
}

object Call {
  def apply(source: Coords, target: Coords, operation: Any): ZIO[Any, Nothing, Call] = {
    for {
      time        <- now
      responseRef <- Ref.make[CallResponse](CallResponse.NotCompleted)
    } yield new Call(CallSite(source, target, operation, time), responseRef)
  }
}
