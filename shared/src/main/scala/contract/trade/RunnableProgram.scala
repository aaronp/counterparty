package contract.trade

import zio.*
import contract.*
import scala.reflect.ClassTag
import java.util.concurrent.TimeUnit

/** This trait is a kind of convenience wrapper around our 'Program' ADT, providing the common (but
  * perhaps unfamiliar or confusing) 'foldMap' machinery we need to execute our program.
  */
trait RunnableProgram[F[_]] {

  type Invoke[A] = (Coords, () => Task[A])

  val calls = Ref.unsafe.make(CallStack())

  /** How do we want to handle the execution of our program? We'll use ZIO Tasks for this.
    *
    * @param operation
    * @return
    */
  def onInput[A](operation: F[A]): Task[A] | Invoke[A]

  private def traceOnInput[A](operation: F[A]): Task[A] = {

    val call = ujson.Obj(
      "name"      -> sourcecode.Name(),
      "pck"       -> sourcecode.Pkg(),
      "fullName"  -> sourcecode.FullName(),
      "enclosing" -> sourcecode.Enclosing()
    )

    def run = onInput(operation) match {
      case task: Task[A]       => task
      case (call: Coords, job) =>

    }

    for {
      now    <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _      <- Console.printLine(s"on $operation")
      result <- run
    } yield result
  }

  extension [A: ClassTag](target: A) {

    def targetName = summon[ClassTag[A]].runtimeClass.getSimpleName

    /** By using 'trace', we can capture: (1) when any operation is called, (2) when a target
      * service is invoked (w/ parameters and its return value), w/ the caveat that we'd want to tap
      * Tasks (3) our entry points 'e.g. placeOrder' should also add a trace entry for the service
      * name and it's first operation
      *
      * @param B
      * @param f
      * @return
      */
    def trace[B](f: A => B) = {
      val retVal = f(target)
      retVal
    }
  }

  // an implicit (given) natural transformation from our program (F[_]) to an effect type (Task[_]) is
  // needed for foldMap to work in our 'run' method
  private given ~>[F, Task] with {
    override def apply[A](fa: F[A]): Task[A] = traceOnInput(fa)
  }

  def run[A](fa: Program[F, A]): Task[A] = fa.foldMap[Task]
}
