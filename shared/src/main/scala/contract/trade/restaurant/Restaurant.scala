package contract.trade.restaurant

import contract.trade.*
import zio.*
import contract.*
import contract.RunnableProgram

trait Restaurant {
  def placeOrder(order: Order): Task[OrderId | OrderRejection]
}

object Restaurant {

  def apply(how: [A] => RestaurantLogic[A] => Result[A])(using telemetry: Telemetry = Telemetry()) =
    new Restaurant with RunnableProgram[RestaurantLogic] {

      override def appCoords                                     = Coords(this)
      override def onInput[T](op: RestaurantLogic[T]): Result[T] = how(op)

      override def placeOrder(order: Order): Task[OrderId | OrderRejection] = run(
        RestaurantLogic.placeOrder(order)
      )
    }

  def default(using telemetry: Telemetry = Telemetry()) = new Restaurant
    with RunnableProgram[RestaurantLogic] {

    override def appCoords = Coords(this)

    override def onInput[A](op: RestaurantLogic[A]) = op match {
      case RestaurantLogic.CheckInventory(ingredients) =>
        Result.TraceTask(
          Coords("inventory", "InventoryService"),
          ZIO.succeed(Inventory(ingredients))
        )
      // Coords("inventory", "InventoryService") -> ZIO.succeed(Inventory(ingredients))
      case RestaurantLogic.MakeDish(dish) =>
        ZIO.succeed(PreparedOrder(dish, OrderId("1"))).asResult
      case RestaurantLogic.UpdateInventory(newInventory) =>
        Result.TraceTask(Coords("inventory", "InventoryService"), ZIO.succeed(()))
      case RestaurantLogic.ReplaceStock(ingredients) =>
        Result.TraceTask(Coords("supplier", "Marketplace"), ZIO.succeed(OrderId("1")))
      case RestaurantLogic.Log(message) =>
        ZIO.succeed(()).asResult
      case RestaurantLogic.NoOp =>
        ZIO.succeed(()).asResult
      case RestaurantLogic.GetStrategy => ZIO.succeed(Strategy(3, 7)).asResult
    }

    override def placeOrder(order: Order): Task[OrderId | OrderRejection] = run(
      RestaurantLogic.placeOrder(order)
    )
  }

  def main(args: Array[String]) = println("hi")
}
