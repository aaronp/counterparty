package contract.trade.market

import contract.*
import support.*

import contract.trade.*
import contract.trade.market.MarketplaceLogic.*
import scala.concurrent.duration.*

class MarketplaceTestData(using telemetry: Telemetry = Telemetry()) extends MarketplaceTestLogic {

  val underTest = Marketplace(defaultLogic).withOverride { case GetConfig =>
    Settings(1.seconds, Address("Override", "Street", "Eyam")).asResultTraced(
      Marketplace.Symbol.withName("Config")
    )
  }

  val basket: Map[Item, Quantity] = Map(
    ("eggs".asItem: Item)    -> (3.asQuantity: Quantity),
    ("brocoli".asItem: Item) -> (1.asQuantity: Quantity)
  )

  val input = Order(basket, Address("unit", "test", "ave"))
  lazy val result =
    underTest.placeOrder(input).execOrThrow()
}
