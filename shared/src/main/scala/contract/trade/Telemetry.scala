package contract.trade

import contract.*
import zio.*

import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag

trait Telemetry(val callsStackRef: Ref[CallStack]) {

  def calls: UIO[Seq[CompletedCall]] = {
    for {
      callStack <- callsStackRef.get
      calls <- ZIO.foreach(callStack.calls) { call =>
        call.response.get.map { resp =>
          CompletedCall(call.invocation, resp)
        }
      }
    } yield calls
  }

  def onCall[F[_], A](source: Coords, target : Coords, operation : F[A]): ZIO[Any, Nothing, Call] = {
    for {
      call <- Call(source, target, operation)
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
  def default(app: String): Coords = Coords("default", app)
  def apply[A: ClassTag](svc: A): Coords = apply[A]

  def apply[A: ClassTag]: Coords = {
    val fullyQualifiedName = summon[ClassTag[A]].runtimeClass.getName
    val parts = fullyQualifiedName.split("\\.", -1).toSeq
    val packageName = parts.init.last
    val name = parts.last.takeWhile(_ != '$')

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

final case class CompletedCall(invocation: CallSite, response: CallResponse) {
  export invocation.*
  def atDateTime = java.time.Instant.ofEpochMilli(timestamp)

  def duration = response match {
    case CallResponse.Error(end, _) => (end - timestamp).millis
    case CallResponse.Completed(end, _) => (end - timestamp).millis
    case CallResponse.NotCompleted => Duration.Infinity
  }

  def resultText = response match {
    case CallResponse.Error(_, err) => s"failed with $err"
    case CallResponse.Completed(_, result) => s"returned $result"
    case CallResponse.NotCompleted => "never completed"
  }
  override def toString = {
    s"$source --[ $operation ]--> $target $resultText and took $duration at $atDateTime"
  }
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
final case class Call(
    invocation: CallSite,
    response: Ref[CallResponse]
) {
  def completeWith[A](result: Task[A]): Task[A] = {
    for {
      either <- result.either
      now    <- Clock.currentTime(TimeUnit.MILLISECONDS)
      result <- either match {
        case Left(err) =>
          for {
            _      <- response.set(CallResponse.Error(now, err))
            failed <- ZIO.fail(err)
          } yield failed
        case Right(ok) =>
          response.set(CallResponse.Completed(now, ok)).as(ok)
      }
    } yield result
  }
}

object Call {
  def apply(source: Coords, target: Coords, operation : Any): ZIO[Any, Nothing, Call] = {
    for {
      now         <- Clock.currentTime(TimeUnit.MILLISECONDS)
      responseRef <- Ref.make[CallResponse](CallResponse.NotCompleted)
    } yield new Call(CallSite(source, target, operation, now), responseRef)
  }
}
