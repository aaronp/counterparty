package contract.trade.restaurant

import contract.*
import contract.trade.*

class RestaurantTestData(using telemetry: Telemetry = Telemetry()) extends RestaurantDefaultLogic {

  val restaurant = Restaurant(defaultLogic)

  val pancake = Dish(
    Ingredient("milk"),
    Ingredient("butter"),
    Ingredient("flour"),
    Ingredient("egg"),
    Ingredient("egg"),
    Ingredient("egg")
  )

  val fishAndChips = Dish(Ingredient("fish"), Ingredient("chips"))

  val order = Order(List(pancake, fishAndChips))

  lazy val result = restaurant.placeOrder(order).execOrThrow()

  def calls: Seq[CompletedCall] = telemetry.calls.execOrThrow()

}
