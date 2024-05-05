package contract.trade

import zio.*
import contract.*
import scala.reflect.ClassTag

trait ProgramApp[F[_]] {

  def onInput[A](operation: F[A]): Task[A]

  extension [A: ClassTag](target: A) {



    def targetName = summon[ClassTag[A]].runtimeClass.getSimpleName


    /**
      * By using 'trace', we can capture:
      * (1) when any operation is called,
      * (2) when a target service is invoked (w/ parameters and its return value), w/ the caveat that we'd want to tap Tasks
      * (3) our entry points 'e.g. placeOrder' should also add a trace entry for the service name and it's first operation
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

  private given ~>[F, Task] with {
    override def apply[A](fa: F[A]): Task[A] = onInput(fa)
  }

  def run[A](fa: Program[F, A]): Task[A] = fa.foldMap[Task]
}
