package contract.trade.restaurant

import contract.*
import contract.trade.*

object RestaurantDefaultLogic extends RestaurantDefaultLogic

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
        OrderId(s"order-${ingredients.size}")
          .asResultTraced(Actor("supplier", "Marketplace").service)
      case RestaurantLogic.Log(message) => ().asResult
      case RestaurantLogic.NoOp         => ().asResult
      case RestaurantLogic.GetStrategy =>
        Strategy(3, 7).asResultTraced(Actor.service[Restaurant])
      case RestaurantLogic.SaveOrder(order) =>
        OrderId(s"order-${order.dishes.size}").asResultTraced(Actor.service[Restaurant])
    }
  }
}
