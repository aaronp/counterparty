package contract.trade.restaurant

import contract.*
import support.*
import contract.trade.*

class RestaurantTestData(using telemetry: Telemetry = Telemetry()) extends RestaurantDefaultLogic {

  val restaurant = Restaurant(defaultLogic)

  lazy val result = restaurant.placeOrder(OrderData.testOrder.asOrder).execOrThrow()

  def calls: Seq[CompletedCall] = telemetry.calls.execOrThrow()

}
