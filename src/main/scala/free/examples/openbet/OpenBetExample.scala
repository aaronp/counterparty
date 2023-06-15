package free.examples.openbet

opaque type CustId = String

/** Some simple data types */
object CustId {
  def apply(id: String): CustId = id
}

case class FeatureFlags(isLoadTest: Boolean, isExample: Boolean)

case class UserData(customerId: CustId, isSelfExcluded: Boolean)


/**
 * Our vocabulary - the set of all the external operations we need in our program
 *
 * @tparam A
 */
sealed trait Operation[A]

/** Check our feature toggles */
case object GetFeatureFlags extends Operation[FeatureFlags]
/** Log a message. In practice this is also a useful no-op */
case class Log(message: String) extends Operation[Unit]
/** does what it says on the tin */
case class GetCustomerById(customerId: CustId) extends Operation[UserData]
/** check a third-party system (GamStop) */
case class CheckGamStop(customerId: CustId) extends Operation[Option[UserData]]
/** save a result */
case class WriteSelfExclusion(customerId: CustId, selfExcluded: Boolean) extends Operation[Unit]

/**
 * This is our imperative program. It has a relative high cyclomatic complexity - many different branches through the code
 *
 * By writing ("lifting") this code into values rather than an imperative style gives us the opportunity to interrogate it/play with it.
 *
 * The original, imperative code might've looked something like this:
 *
 * {{{
 *   def myProgram(featureFlags : FeatureFlags, customerId : String) {
 *     if (featureFlags.isLoadTest) {
 *       println("load test - ignoring")
 *     } else {
 *        val customerStatus = checkGamStop(customerId)
 *        val isSelfExcluded = (customerStatus != null) && customerStatus.isSelfExcluded
 *        if (isSelfExcluded) {
 *          println("customer is self excluded")
 *        } else {
 *          val customer = getCustomerById(customerId)
 *          setSelfExclusionStatus(customerId, customer.isSelfExcluded)
 *        }
 *     }
 *   }
 * }}}
 */
object App {

  import free.*

  def updateUser(customerId: CustId): Free[Operation, Unit] = {
    def checkGamStop: Free[Operation, Boolean] = CheckGamStop(customerId).freeM.flatMap {
      case Some(found) => Free.pure(found.isSelfExcluded)
      case None => Free.pure(false)
    }

    def checkOpenBetAndClobberResult: Free[Operation, Unit] = for {
      openBetUser <- GetCustomerById(customerId).freeM
      _ <- WriteSelfExclusion(customerId, openBetUser.isSelfExcluded).freeM
    } yield ()

    def checkUser: Free[Operation, Unit] = for {
      isSelfExcluded <- checkGamStop
      _ <- if isSelfExcluded then Log(s"User $customerId is already self-excluded").freeM else checkOpenBetAndClobberResult
    } yield ()

    for {
      flags <- GetFeatureFlags.freeM
      _ <- if flags.isLoadTest then Log("Ignoring - load test").freeM else checkUser
    } yield ()
  }
}
