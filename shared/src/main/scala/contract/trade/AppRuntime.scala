package contract.trade

import contract.*
import contract.diagram.CallsState

import scala.reflect.ClassTag
import zio.*

import scala.util.*
import scala.annotation.targetName

/** The idea floating around is if there's a way to represent a kind of operation look-up for all
  * the actors in the system, and use a ZIO Scope to capture the trace side-effects.
  *
  * That way people could just write their IMPLs in terms of ZIO operations, and we could get all
  * the tracing, sequence diagrams, etc all for free.
  *
  * We'd also be able to easily mix the impl logic with our side-effect logic, reduce the number of
  * interpreters we'd have to write, and also perhaps even get some nice test-case generation or
  * "turn on verbose mode" for free in our actual running code
  */
class AppRuntime[F[_]] {

  type H[A] = PartialFunction[F[A], Task[A]]

  private var handlers: Seq[H[Any]] = Nil

  @targetName("createDraftLogicAsMermaid")
  given ~>[F, Task] with {
    override def apply[A](fa: F[A]): Task[A] = handlers
      .filter { h =>
        Try(h.isDefinedAt(fa.asInstanceOf[F[Any]])).getOrElse(false)
      }
      .headOption
      .getOrElse(sys.error(s"BUG: you didn't register a handler for $fa"))
      .apply(fa.asInstanceOf[F[Any]])
      .asInstanceOf[Task[A]]
  }

  def run[A](app: Program[F, A]): ZIO[Any, Throwable, A] = app.foldMap[Task]

  def register[A](handler: PartialFunction[F[A], Task[A]]) = {
    handlers = handler.asInstanceOf[H[Any]] +: handlers
  }
}
