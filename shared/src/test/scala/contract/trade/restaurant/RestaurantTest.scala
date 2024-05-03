package contract.trade.restaurant

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import contract.*
import contract.trade.*
import zio.*

class RestaurantTest extends AnyWordSpec with Matchers {

  "Restaurant.placeOrder" should {
    "restock when we get low on stuff" in {
      // val restaurant = Restaurant.apply {
      //   case RestaurantLogic.CheckInventory(ingredients) =>
      //     Coords("inventory", "InventoryService") -> ZIO.succeed(Inventory(ingredients))
      //   case RestaurantLogic.MakeDish(dish) =>
      //     ZIO.succeed(PreparedOrder(dish, OrderId("1")))
      //   case RestaurantLogic.UpdateInventory(newInventory) =>
      //     Coords("inventory", "InventoryService") -> ZIO.succeed(())
      //   case RestaurantLogic.ReplaceStock(ingredients) =>
      //     Coords("supplier", "Marketplace") -> ZIO.succeed(OrderId("1"))
      //   case RestaurantLogic.Log(message) =>
      //     ZIO.succeed(())
      //   case RestaurantLogic.NoOp =>
      //     ZIO.succeed(())
      //   case RestaurantLogic.GetStrategy =>
      //     ZIO.succeed(Strategy(3, 7))
      // }

      given telemetry: Telemetry = Telemetry()

      val restaurant: Restaurant = ??? // Restaurant.default()

      val pancake = Dish(
        Ingredient("milk"),
        Ingredient("butter"),
        Ingredient("flour"),
        Ingredient("egg"),
        Ingredient("egg"),
        Ingredient("egg")
      )

      val fishAndChips = Dish(Ingredient("fish"), Ingredient("chips"))

      val order  = Order(List(pancake, fishAndChips))
      val result = restaurant.placeOrder(order).execOrThrow()

      val calls = telemetry.calls.execOrThrow()
      calls.foreach(println)

      result shouldBe Right(OrderId("1"))
    }
  }
}
