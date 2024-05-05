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

  def onCall(source: Coords): ZIO[Any, Nothing, Call] = {
    for {
      call <- Call(source)
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
final case class Coords(namespace: String, app: String)
object Coords {
  def default(app: String): Coords = Coords("default", app)
  def apply[A: ClassTag](svc: A): Coords = {
    val name = summon[ClassTag[A]].runtimeClass.getSimpleName
    val pck  = summon[ClassTag[A]].runtimeClass.getName
    new Coords(pck, name)
  }
  def apply[A: ClassTag]: Coords = {
    val name = summon[ClassTag[A]].runtimeClass.getSimpleName
    val pck  = summon[ClassTag[A]].runtimeClass.getName
    new Coords(pck, name)
  }
}

enum CallResponse:
  case NotCompleted
  case Error(timestamp: Long, bang: Any)
  case Completed(timestamp: Long, result: Any)

final case class CallSite(
    source: Coords,
    //    target: Coords,
    //    operations: String,
    //    input: Any,
    timestamp: Long
)

final case class CompletedCall(invocation: CallSite, response: CallResponse)

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
  def apply(source: Coords): ZIO[Any, Nothing, Call] = {
    for {
      _           <- Console.printLine(s"creating call for $source").orDie
      now         <- Clock.currentTime(TimeUnit.MILLISECONDS)
      responseRef <- Ref.make[CallResponse](CallResponse.NotCompleted)
    } yield new Call(CallSite(source, now), responseRef)
  }
}
