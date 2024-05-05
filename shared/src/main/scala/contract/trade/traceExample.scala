package contract.trade
import zio.*
import scala.concurrent.duration.*
import contract.*

/** TODO -
  *
  * Make these services Programs, and then have their traits extends ProgramApp.
  *
  * See how using 'trace' could work, where we track the source/target of each call (sent and
  * received w/ time)
  *
  * Give each ProgramApp an EventBus to send the calls to (w/ timestamps), and see if we can derive
  * the mermaid flow from that
  *
  * NOTICE TO:
  *
  * We *should* be able to compose different services w/o having to know about each and every
  * dependency as per here
  */

trait SvcA {
  def run(x: Int): Task[String]
}

object SvcA {
  def apply(handle: Int => Task[String]): SvcA = new SvcA {
    def run(x: Int): Task[String] = handle(x)
  }
}

trait SvcB {
  def echo(x: String): Task[String]
}
object SvcB {
  def apply(c: SvcC = SvcC(), d: SvcD = SvcD()): SvcB = new SvcB {

    // : Schedule[Any, Throwable, Long]
    val retrySchedule =
      Schedule.recurs(3) && Schedule.exponential(1.second)

    def echo(x: String) = {
      val app = for {
        x <- c.nextNum()
        y <- d.nextNum()
      } yield s"echo : $x / $y is ${x / y}"

      val retVal = app
        .retry(retrySchedule)
        .tapError { error =>
          Console.printError(s"Retry failed with error: $error")
        }
        .orDie

      retVal
    }
  }
}

trait SvcC {
  def nextNum(): Task[Int]
}
object SvcC {
  def apply(): SvcC = new SvcC {
    val ref: Ref[Int] = Ref.make(3).execOrThrow()
    def nextNum(): Task[Int] = {
      for {
        x <- ref.get
        _ <- ref.update {
          case 3 => 7
          case 7 => 0
          case 0 => 3
        }
      } yield x
    }
  }
}

trait SvcD {
  def nextNum(): Task[Int]
}
object SvcD {
  def apply(): SvcD = new SvcD {
    val ref: Ref[Int] = Ref.make(13).execOrThrow()
    def nextNum(): Task[Int] = {
      for {
        x <- ref.get
        _ <- ref.update {
          case 13 => 0
          case 11 => 13
          case 0  => 11
        }
      } yield x
    }
  }
}

object App {

  /** In our example, we want to be able to wire up A -> B and B knows about C -> D, and we want to
    * be able to trace the call stack
    *
    * @param args
    */
  def main(args: Array[String]) = {}
}
