package free.example

opaque type CustId = String
object CustId {
  def apply(id : String) : CustId = id
}

case class FeatureFlags(isLoadTest :Boolean, isExample :Boolean)
case class UserData(customerId : CustId, isSelfExcluded : Boolean)

sealed trait Operation[A]

case object GetFeatureFlags extends Operation[FeatureFlags]
case class ReadUserData(customerId : CustId) extends Operation[UserData]
case class CheckOpenBest(customerId : CustId) extends Operation[UserData]
case class WriteSelfExclusion(customerId : CustId, selfExcluded: Boolean) extends Operation[UserData]

object App {
  import free.*
  def apply(): Free[Operation, FeatureFlags] = {
    for {
      flags <- GetFeatureFlags.freeM
    } yield flags
  }
}
