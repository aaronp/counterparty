package contract.trade

import zio.*
import contract.*

import scala.annotation.targetName
import scala.reflect.ClassTag

/** This trait is a kind of convenience wrapper around our 'Program' ADT, providing the common (but
  * perhaps unfamiliar or confusing) 'foldMap' machinery we need to execute our program.
  */
trait RunnableProgram[F[_]](val telemetry: Telemetry) {

  /**
   * Run will execute this program using the 'onInput' to determine how to implement each operation
   *
   * @param fa the program to run
   * @tparam A the result type
   * @return a runnable Task
   */
  def run[A](fa: Program[F, A]): Task[A] = fa.foldMap[Task]

  type Invoke[A] = (Coords, Task[A])

  /**
   * we need to get at the subclass 'Coords', not this trait's name (otherwise everything will just look like a RunnableProgram)
   * @return the 'coords' (category and service name) of this application. Typically just return 'Coords(this)' in sublasses
   */
  protected def appCoords : Coords

  /** How do we want to handle the execution of our program?
    *
    * @param operation the input operation
    * @return either a simple task if it's done in-process, or an 'Invoke' if it relies on calling another service
    */
  def onInput[A](operation: F[A]): Task[A] | Invoke[A]

  /**
   * Our 'traceOnInput' will add a call to our telemetry, giving us insight on the operations of our program
   * @param operation the operation to run
   * @tparam A the return type
   * @return the Task result of this operation, with the side-effect of tracking the invocation
   */
  private def traceOnInput[A](operation: F[A]): Task[A] = {
    val (targetCoords, run) = onInput(operation) match {
      case task: Task[A]     => (appCoords, task)
      case invoke: Invoke[A] => invoke
    }

    for {
      call   <- telemetry.onCall(appCoords, targetCoords, operation)
      result <- call.completeWith(run)
    } yield result
  }

  // an implicit (given) natural transformation from our program (F[_]) to an effect type (Task[_]) is
  // needed for foldMap to work in our 'run' method
  @targetName("programAsTask")
  private given ~>[F, Task] with {
    override def apply[A](fa: F[A]): Task[A] = traceOnInput(fa)
  }

}
