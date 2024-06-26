package contract.trade.market

import zio.*
import scala.concurrent.duration.*
import contract.*
import contract.trade.market.MarketplaceLogic.*
import zio.ZIO
import support.ActorType

object MarketplaceTestLogic extends MarketplaceTestLogic

trait MarketplaceTestLogic {

  private val everythingIs100: Distributor = "everything's £100".asDistributor
  private def everythingIs100Coords = Actor.person("distributor", everythingIs100.distributorName)
  private val vowelsAreAFiver: Distributor = "vowels are £5".asDistributor
  private def vowelsAreAFiverCoords = Actor.person("distributor", vowelsAreAFiver.distributorName)

  extension (char: Char) {
    def isVowel: Boolean = "aeiou".contains(char.toLower)
  }

  def quoteVowels(order: Order) = {
    val prices: Map[Item, Price] = order.basket.filter(_._1.name.head.isVowel).map {
      case (item: Item, _) => (item -> (5.asPrice: Price))
    }
    prices
  }

  def everythings100(order: Order) = {
    val prices: Map[Item, Price] = order.basket.map { case (item: Item, _) =>
      (item -> (100.asPrice: Price))
    }
    prices
  }

  def MarketDB = Marketplace.Symbol.withName("DB").withType(ActorType.Database)

  def defaultLogic(using telemetry: Telemetry): [A] => MarketplaceLogic[A] => Result[A] =
    [A] => {
      (_: MarketplaceLogic[A]) match {
        case GetConfig =>
          Settings(2.seconds, Address("1", "2", "3")).asResultTraced(
            Marketplace.Symbol.withName("Config").withType(ActorType.FileSystem)
          )
        case SaveOrder(order) =>
          (order.hashCode().toString.asOrderId: OrderId)
            .asResultTraced(MarketDB)
        case SaveDistributors(orderId, sentTo) =>
          ().asResultTraced(MarketDB)
        case input @ SendOutRequestsForQuote(order) =>
          // here we explicitly trace the calls we make, as they are internal to the ultimate 'Task' we return
          // that is to say, there is a granularity here we don't want to lose
          ZIO
            .foreachPar(Seq(vowelsAreAFiver, everythingIs100)) {
              case distributor @ `vowelsAreAFiver` =>
                RFQResponse(distributor, quoteVowels(order)).asTaskTraced(
                  Marketplace.Symbol,
                  vowelsAreAFiverCoords,
                  order
                )

              case distributor @ `everythingIs100` =>
                RFQResponse(distributor, everythings100(order)).asTaskTraced(
                  Marketplace.Symbol,
                  everythingIs100Coords,
                  order
                )
            }
            .taskAsResult // we don't want to trace the market -> market wrapper job, so just return a normal result
        case input @ SendOrders(orders) =>
          val listResult = ZIO.foreachPar(orders) {
            case DistributorOrder(distributor, orderPortion, orderId) =>
              val ref: DistributorRef = s"$orderId-${orderPortion.hashCode()}".asDistributorOrderRef
              val tuple               = (distributor -> ref)

              // again, here we explicitly trace the calls we make, as they are internal to the ultimate 'Task' we return
              // that is to say, there is a granularity here we don't want to lose
              orderPortion
                .asTaskTraced(
                  Marketplace.Symbol,
                  Actor.person("distributor", distributor.distributorName),
                  input
                )
                .as(tuple) // <-- we trace this task, but ultimately return this from our function
          }
          listResult.map(_.toMap).taskAsResult
      }
    }
}
