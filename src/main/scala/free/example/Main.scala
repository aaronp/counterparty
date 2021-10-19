package free.example

import free.~>

import scala.util.Try

@main def run(customerId: String, loadTest: Boolean) =
  val flags = FeatureFlags(loadTest, false)
  given logic: ~>[Operation, Try] = Synchronous(flags, _ => HttpResponse(200, "selfExcluded"))
  App.updateUser(CustId(customerId)).foldMap[Try]