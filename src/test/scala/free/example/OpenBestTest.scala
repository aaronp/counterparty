package free.example

import free.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OpenBestTest extends AnyWordSpec with Matchers {

  // for each combination of flags/results...
  for {
    isLoadTest <- List(false, true)
    openBetResults <- List(false, true)
    gamstopResults <- List(None, Option(false), Option(true))
  } {
    /** Our business logic (our 'mock') results
     */
    given~>[Operation, BufferState] with
      def apply[A](operation: Operation[A]): BufferState[A] =
        val logic: Operation[A] => BufferState[A] = testScenario {
          case GetFeatureFlags => FeatureFlags(isLoadTest, true)
          case GetCustomerById(customerId) => UserData(customerId, openBetResults)
          case CheckGamStop(customerId) => gamstopResults.map(flag => UserData(customerId, flag))
        }
        logic(operation)


    val testScenarioDescription = {
      val gamstopDesc = gamstopResults match {
        case None => "not found"
        case Some(true) => "self-excluded"
        case Some(false) => "not self-excluded"
      }
      s"Given load test is '${if isLoadTest then "on" else "off"}', a user is${if openBetResults then "" else "not"} self-excluded in OpenBet, and the customer is $gamstopDesc according to gam-stop"
    }

    // our test...
    testScenarioDescription should {
      "work as expected ;-) with App.updateUser" in {
        val codeUnderTest: Free[Operation, Unit] = App.updateUser(CustId("foo"))
        
        codeUnderTest.foldMap[BufferState] match {
          case State(run) =>
            val (_, testLog) = run(Buffer(Nil))

            println(s"$testScenarioDescription: $testLog")
            println("-" * 120)
        }
      }
    }
  }

  def testScenario[A](pf: PartialFunction[Operation[A], A]) = {
    val handler = (_: Operation[A]) match {
      case op if pf.isDefinedAt(op) => pf(op)
      case GetFeatureFlags => FeatureFlags(false, true)
      case GetCustomerById(customerId) => UserData(customerId, false)
      case CheckGamStop(customerId) => Some(UserData(customerId, true))
      case WriteSelfExclusion(_, _) => ()
      case Log(_) => ()
    }

    (op: Operation[A]) => {
      handler(op) match {
        case result: A => State.combine[Buffer, A](Buffer(op, result), result)
      }
    }
  }
}