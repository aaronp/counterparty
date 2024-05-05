package contract.trade.restaurant

import contract.trade.*
import zio.*
import contract.*

trait Restaurant {
  def placeOrder(order: Order): Task[OrderId | OrderRejection]
}

object Restaurant {

  def apply(): Restaurant = new Restaurant with ProgramApp[RestaurantLogic] {

    override def onInput[A](op: RestaurantLogic[A]) = op match {
      case RestaurantLogic.CheckInventory(ingredients) =>
        val name      = sourcecode.Name()
        val fullName  = sourcecode.FullName()
        val enclosing = sourcecode.Enclosing()

        ZIO.succeed(Inventory(ingredients))

      case RestaurantLogic.MakeDish(dish) =>
        ZIO.succeed(PreparedOrder(dish, OrderId("1")))

      case RestaurantLogic.UpdateInventory(newInventory) =>
        ZIO.succeed(())
      case RestaurantLogic.ReplaceStock(ingredients) =>
        ZIO.succeed(OrderId("1"))
      case RestaurantLogic.Log(message) =>
        ZIO.succeed(())
      case RestaurantLogic.NoOp =>
        ZIO.succeed(())
      case RestaurantLogic.GetStrategy =>
        ZIO.succeed(Strategy(3, 7))

    }

    def placeOrder(order: Order): Task[OrderId | OrderRejection] = run {
      RestaurantLogic.placeOrder(order)
    }
  }

  def main(args: Array[String]) = println("hi")
}
