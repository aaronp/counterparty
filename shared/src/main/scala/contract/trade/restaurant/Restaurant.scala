package contract.trade.restaurant

import contract.trade.*
import zio.*
import contract.*

trait Restaurant {
  def placeOrder(order: Order): Task[OrderId | OrderRejection]
}

object Restaurant {

  def apply(telemetry: Telemetry = Telemetry()) = new Restaurant
    with RunnableProgram[RestaurantLogic](telemetry) {

    override def appCoords = Coords(this)

    override def onInput[A](op: RestaurantLogic[A]) = op match {
      case RestaurantLogic.CheckInventory(ingredients) =>
        Coords("inventory", "InventoryService") -> ZIO.succeed(Inventory(ingredients))
      case RestaurantLogic.MakeDish(dish) =>
        ZIO.succeed(PreparedOrder(dish, OrderId("1")))
      case RestaurantLogic.UpdateInventory(newInventory) =>
        Coords("inventory", "InventoryService") -> ZIO.succeed(())
      case RestaurantLogic.ReplaceStock(ingredients) =>
        Coords("supplier", "Marketplace") -> ZIO.succeed(OrderId("1"))
      case RestaurantLogic.Log(message) =>
        ZIO.succeed(())
      case RestaurantLogic.NoOp =>
        ZIO.succeed(())
      case RestaurantLogic.GetStrategy =>
        ZIO.succeed(Strategy(3, 7))

    }

    override def placeOrder(order: Order): Task[OrderId | OrderRejection] = run(
      RestaurantLogic.placeOrder(order)
    )

  }

  def main(args: Array[String]) = println("hi")
}
