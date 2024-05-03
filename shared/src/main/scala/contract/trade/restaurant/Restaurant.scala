package contract.trade.restaurant

import contract.trade.AppRuntime

object Restaurant {

  import RestaurantLogic.*
  val app = new AppRuntime[RestaurantLogic]

  val ok = app.register { case MakeDish(dish) =>
    zio.ZIO.attempt(println("making dish"))
  }

  def main(args: Array[String]) = {
    println("hi")
  }
}
