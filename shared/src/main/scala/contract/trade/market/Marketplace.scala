package contract.trade.market

import zio.*
import contract.*
import contract.RunnableProgram
import concurrent.duration.*

trait Marketplace:
  def placeOrder(order: Order): Task[OrderId | OutOfStockResponse]

object Marketplace {
  import MarketplaceLogic.*

  class App(logic: [A] => MarketplaceLogic[A] => Result[A])(using telemetry: Telemetry)
      extends Marketplace
      with RunnableProgram[MarketplaceLogic] {
    override def appCoords = Coords(this)

    override def onInput[T](op: MarketplaceLogic[T]) = logic(op)

    // TODO - pull this up to RunnableProgram
    def overrideWith(
        pf: PartialFunction[MarketplaceLogic[_], Result[_]]
    ) = {
      val newLogic: [T] => MarketplaceLogic[T] => Result[T] = [T] =>
        (_: MarketplaceLogic[T]) match {
          case op if pf.isDefinedAt(op) =>
            pf(op.asInstanceOf[MarketplaceLogic[T]]).asInstanceOf[Result[T]]
          case op => logic(op)
      }
      new App(newLogic)
    }

    override def placeOrder(order: Order): Task[OrderId | OutOfStockResponse] = run(
      MarketplaceLogic.placeOrder(order)
    )
  }

  def apply(how: [A] => MarketplaceLogic[A] => Result[A])(using
      telemetry: Telemetry = Telemetry()
  ): App = new App(how)
}
