package contract.trade.restaurant

import contract.*
import contract.trade.*
import support.{given, *}

object RestaurantDefaultLogic extends RestaurantDefaultLogic {
  def newRestaurant(using telemetry: Telemetry) = Restaurant(defaultLogic)
}

trait RestaurantDefaultLogic {

  val defaultLogic: [A] => RestaurantLogic[A] => Result[A] = [A] => {
    (_: RestaurantLogic[A]) match {
      case RestaurantLogic.CheckInventory(ingredients) =>
        Inventory(ingredients).asResultTraced(Actor.service("inventory", "InventoryService"))
      case RestaurantLogic.MakeDish(dish) =>
        PreparedOrder(dish, OrderId("1")).asResult
      case RestaurantLogic.UpdateInventory(newInventory) =>
        ().asResultTraced(Actor.service("inventory", "InventoryService"))
      case RestaurantLogic.ReplaceStock(ingredients) =>
        s"replace-${ingredients.size}".asReplacementOrderRef
          .asResultTraced(Actor("supplier", "Marketplace").service)
      case RestaurantLogic.Log(message) =>
        Console.println(message).asResult
      case RestaurantLogic.NoOp => Console.println("no-op").asResult
      case RestaurantLogic.GetStrategy =>
        Strategy(30, 7).asResultTraced(Actor.service[Restaurant])
      case RestaurantLogic.SaveOrder(order) =>
        OrderId(s"order-${order.dishes.size}").asResultTraced(Actor.service[Restaurant])
    }
  }
}
