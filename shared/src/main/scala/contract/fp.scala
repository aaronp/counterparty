//> using scala "3.4.1"
package contract

import scala.util.Try
import zio.*

/** Instead of bringing in an FP library, we just provides a minimal implementation of what we need
  * here
  */

/** Semigroup - a way of combining two of the same type
  * @tparam A
  *   the type to combine
  */
trait Semigroup[A]:
  def combine(a: A, b: A): A
object Semigroup:
  def apply[A](f: (A, A) => A) = new Semigroup[A] {
    override def combine(a: A, b: A): A = f(a, b)
  }

  given [A]: Semigroup[List[A]] with
    def combine(a: List[A], b: List[A]): List[A] = a ++ b

/** The state monad
  * @param run
  *   the state function which can provide an output paired with a new state when giving an initial
  *   state
  * @tparam S
  *   the state type
  * @tparam A
  *   the result type
  */
case class State[S, A](run: S => (A, S)):
  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State { s =>
      val (a, t) = run(s)
      f(a).run(t)
    }

  def map[B](f: A => B): State[S, B] =
    State { s =>
      val (a, t) = run(s)
      (f(a), t)
    }

object State:
  def of[S, A](value: A) = State[S, A](in => (value, in))

  def combine[S: Semigroup, A](state: S, value: A): State[S, A] =
    State[S, A](left => (value, summon[Semigroup[S]].combine(state, left)))

  given generic[S]: Monad[[A] =>> State[S, A]] with
    def pure[A](a: A): State[S, A] = State(s => (a, s))

    def flatMap[A, B](fa: State[S, A], f: A => State[S, B]) = fa.flatMap(f)

end State

/** A simple (no class hierarchies, like Pure[F[_]] or Functor[F[_]]) representation of a Monad
  *
  * @tparam F
  *   the polymorphic type
  */
trait Monad[F[_]]:
  def pure[A](a: A): F[A]

  def flatMap[A, B](fa: F[A], f: A => F[B]): F[B]

object Monad:
  def apply[F[_]](using monad: Monad[F]): Monad[F] = monad

  given Monad[Try] with
    override def pure[A](a: A)                             = Try(a)
    override def flatMap[A, B](fa: Try[A], f: A => Try[B]) = fa.flatMap(f)

  /** An instance of Monad for List */
  given Monad[List] with
    override def pure[A](a: A)                               = List(a)
    override def flatMap[A, B](fa: List[A], f: A => List[B]) = fa.flatMap(f)

  given Monad[Task] with
    override def pure[A](a: A): Task[A] = ZIO.succeed(a)

    override def flatMap[A, B](fa: Task[A], f: A => Task[B]): Task[B] = fa.flatMap(f)

extension [F[_]: Monad, A](fa: F[A])
  def flatMap[B](f: A => F[B]): F[B] = summon[Monad[F]].flatMap(fa, f)

/** Representation of a Natural Transformation - a function from one polymorphic type constructor to
  * another
  *
  * @tparam F
  *   the input type constructor
  * @tparam G
  *   the result type constructor
  */
trait ~>[F[_], G[_]]:
  def apply[A](fa: F[A]): G[A]

enum Program[F[_], A]:
  case Pure(value: A)    extends Program[F, A]
  case Suspend(fa: F[A]) extends Program[F, A]
  case FlatMap[G[_], In, Out](self: Program[G, In], f: In => Program[G, Out])
      extends Program[G, Out]

  final def flatMap[B](f: A => Program[F, B]): Program[F, B] = FlatMap[F, A, B](this, f)

  final def map[B](f: A => B): Program[F, B] = FlatMap[F, A, B](this, a => Program.of(f(a)))

  final def foldMap[G[_]: Monad](using nt: F ~> G): G[A] = this match {
    case Pure(value) => Monad[G].pure(value)
    case Suspend(fa) => nt(fa)
    case FlatMap(inner, f) =>
      val ge = inner.foldMap(using Monad[G], nt)
      Monad[G].flatMap(ge, in => f(in).foldMap(using Monad[G], nt))
  }

object Program:
  def pure[F[_], A](a: A): Program[F, A] = Program.Pure(a)
  def of[F[_], A](a: A): Program[F, A]   = pure[F, A](a)

  // like 'pure' but for 'F[_]' types
  def apply[F[_], A](fa: F[A]): Program[F, A] = Program.Suspend(fa)

extension [F[_], A](fa: F[A]) def asProgram: Program[F, A] = Program(fa)

// ---- for telemetry ----

enum Result[A]:
  case RunTask(job: Task[A])                                             extends Result[A]
  case TraceTask(coords: Actor, job: Task[A], input: Option[Any] = None) extends Result[A]

  def task: Task[A] = this match {
    case RunTask(task)         => task
    case TraceTask(_, task, _) => task
  }

/** A common convenience method for ZIO stuff... might as well stick it here
  */
extension [A](job: Task[A]) {

  def execOrThrow(): A = Unsafe.unsafe { implicit unsafe =>
    Runtime.default.unsafe.run(job).getOrThrowFiberFailure()
  }
  def taskAsResult: Result[A] = Result.RunTask(job)
  def taskAsResultTraced(targetSystem: Actor, input: Any = null): Result[A] =
    Result.TraceTask(targetSystem, job, Option(input))
}

/** Trace this call to the given 'target' service / database / whatever
  *
  * @param target
  *   the business name (Actor) of the target we're calling
  * @param input
  *   the input used in this request
  * @return
  *   a 'pimped' task which will update the telemetry when run
  */
private def traceTask[A](job: Task[A], source: Actor, target: Actor, input: Any)(using
    telemetry: Telemetry
): Task[A] = {
  for
    call   <- telemetry.onCall(source, target, input)
    result <- call.completeWith(job)
  yield result
}

extension [A](op: => A) {
  def asTask: Task[A] = ZIO.attempt(op)

  /** This method is intended to be used when tasks are run within a single operation, for example
    * when we need to run things in parallel for a given input, so want to trace those resulting
    * actions.
    *
    * Everything else will be traced via the 'RunnableProgram' when we encounter an operation
    *
    * @param source
    *   the source system for this call (needed as we can run these tasks outside of
    *   RunnablePrograms)
    * @param target
    *   the target system - who we're calling
    * @param input
    *   the input used to make this call
    * @param telemetry
    *   the telemetry used to track calls
    * @return
    *   a task which will update the telemetry when run
    */
  def asTaskTraced(source: Actor, target: Actor, input: Any)(using
      telemetry: Telemetry
  ): Task[A] = traceTask(
    asTask,
    source,
    target,
    input
  )

  /** @return
    *   this operation as a task inside a 'Result' type. The call will NOT be traced
    */
  def asResult: Result[A] = Result.RunTask(asTask)

  /** @param targetSystem
    *   who are we calling? Another system? Database? Queue? Filesystem? ...
    * @param input
    *   the input involved in this call
    * @return
    *   this operation as a task inside a traced 'Result' type. The call to the target will be
    *   traced when run inside a RunnableProgram
    */
  def asResultTraced(targetSystem: Actor, input: Any = null): Result[A] =
    Result.TraceTask(targetSystem, asTask, Option(input))

}
