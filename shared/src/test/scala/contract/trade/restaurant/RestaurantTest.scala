package contract.trade.restaurant

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import contract.*
import zio.*

class RestaurantTest extends AnyWordSpec with Matchers {

  "Restaurant" should {
    "placeOrder" in {
      val restaurant = Restaurant()

      val order  = Order(List(Dish(List(Ingredient("1")))))
      val result = restaurant.placeOrder(order).execOrThrow()

      result shouldBe Right(OrderId("1"))
    }
  }
}
