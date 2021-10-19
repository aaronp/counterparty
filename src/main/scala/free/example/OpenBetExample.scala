package free.example

opaque type CustId = String

object CustId {
  def apply(id: String): CustId = id
}

case class FeatureFlags(isLoadTest: Boolean, isExample: Boolean)

case class UserData(customerId: CustId, isSelfExcluded: Boolean)


/**
 * Our vocabulary - the set of all the external operations we need to perform
 *
 * @tparam A
 */
sealed trait Operation[A]

case object GetFeatureFlags extends Operation[FeatureFlags]

case class Log(message: String) extends Operation[Unit]

case class CheckOpenBet(customerId: CustId) extends Operation[UserData]

case class CheckGamStop(customerId: CustId) extends Operation[Option[UserData]]

case class WriteSelfExclusion(customerId: CustId, selfExcluded: Boolean) extends Operation[Unit]

object App {

  import free.*

  def updateUser(customerId: CustId): Free[Operation, Unit] = {
    def checkGamStop: Free[Operation, Boolean] = CheckGamStop(customerId).freeM.flatMap {
      case Some(found) => Free.pure(found.isSelfExcluded)
      case None => Free.pure(false)
    }

    def checkOpenBetAndClobberResult: Free[Operation, Unit] = for {
      openBetUser <- CheckOpenBet(customerId).freeM
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
