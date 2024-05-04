package contract.trade.restaurant

import contract.trade.AppRuntime
import zio.*
import contract.*

trait Restaurant {
  def placeOrder(order: Order): Task[OrderId | OrderRejection]
}

trait App[F[_]] {

  def onInput[A]: F[A] => Task[A]

  private given ~>[F, Task] with {
    override def apply[A](fa: F[A]): Task[A] = onInput(fa)
  }

  def run[A](fa: Program[F, A]): Task[A] = fa.foldMap[Task]
}

object Restaurant {

  def apply(handle: [A] => RestaurantLogic[A] => Task[A]): Restaurant = new Restaurant
    with App[RestaurantLogic] {

    override def onInput[A] = handle[A]

    def placeOrder(order: Order): Task[OrderId | OrderRejection] = run(
      RestaurantLogic.placeOrder(order)
    )
  }

  def main(args: Array[String]) = println("hi")
}
