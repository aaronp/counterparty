package contract.trade.market

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import contract.*
import contract.trade.*
import zio.*
import scala.concurrent.duration.*

import contract.{given, *}
import scala.language.implicitConversions

class MarketplaceTest extends AnyWordSpec with Matchers {

  import MarketplaceLogic.*

  private val everythingIs100: Distributor = "everything's £100".asDistributor
  private val vowelsAreAFiver: Distributor = "vowels are £5".asDistributor

  extension (char: Char) {
    def isVowel: Boolean = "aeiou".contains(char.toLower)
  }

  def quoteVowels(order: Order) = {
    val prices: Map[Item, Price] = order.basket.filter(_._1.name.head.isVowel).map {
      case (item: Item, _) => (item -> (5.asPrice: Price))
    }
    // RFQResponse(vowelsAreAFiver,
    prices.toMap
  }

  def everythings100(order: Order) = {
    val prices: Map[Item, Price] = order.basket.map { case (item: Item, _) =>
      (item -> (100.asPrice: Price))
    }
    // everythingIs100
    prices.toMap
  }

  def defaultLogic(using telemetry: Telemetry): [A] => MarketplaceLogic[A] => Result[A] =
    [A] => {
      (_: MarketplaceLogic[A]) match {
        case GetConfig =>
          Result.TraceTask(
            Coords("marketplace", "Config"),
            ZIO.succeed(Settings(2.seconds, Address("1", "2", "3")))
          )
        case SaveOrder(order) =>
          Result.TraceTask(
            Coords("marketplace", "DB"),
            ZIO.succeed(order.hashCode().toString.asOrderId)
          )
        case SaveDistributors(orderId, sentTo) =>
          Result.TraceTask(Coords("marketplace", "DB"), ZIO.succeed(()))
        case input @ SendOutRequestsForQuote(order) =>
          ZIO.foreachPar(Seq(vowelsAreAFiver, everythingIs100)) {
            case distributor @ `vowelsAreAFiver` =>
              val job = ZIO.succeed(RFQResponse(distributor, quoteVowels(order)))
              Coords[Marketplace].trace(job, Coords("distributor", "VowelsAre£5"), input)
            case distributor @ `everythingIs100` =>
              val job = ZIO.succeed(RFQResponse(distributor, everythings100(order)))
              Coords[Marketplace].trace(job, Coords("distributor", "Everything's £100"), input)
          }
        case input @ SendOrders(orders) =>
          //   val result: Seq[(Distributor, DistributorRef)] = orders.map {
          //     case DistributorOrder(distributor, orderPortion, orderId) =>
          //       val ref: DistributorRef = s"$orderId-${orderPortion.hashCode()}".asDistributorOrderRef
          //       (distributor -> ref)
          //   }
          //   // TODO - parallelize this and trace
          //   ZIO.succeed(result.toMap)

          val listResult = ZIO.foreachPar(orders) {
            case DistributorOrder(distributor, orderPortion, orderId) =>
              val ref: DistributorRef = s"$orderId-${orderPortion.hashCode()}".asDistributorOrderRef
              val tuple               = (distributor -> ref)
              Coords[Marketplace].trace(
                ZIO.succeed(tuple),
                Coords("distributor", distributor.distributorName),
                input
              )
          }
          listResult.map(_.toMap)
      }
    }

  "Marketplace" should {
    "trace orders" in {

      given telemetry: Telemetry = Telemetry()

      val underTest = Marketplace(defaultLogic).overrideWith { case GetConfig =>
        Result.TraceTask(
          Coords("marketplace", "Config"),
          ZIO.succeed(Settings(1.seconds, Address("Override", "Street", "Eyam")))
        )
      }

      val basket: Map[Item, Quantity] = Map(
        ("eggs".asItem: Item)    -> (3.asQuantity: Quantity),
        ("brocoli".asItem: Item) -> (1.asQuantity: Quantity)
      )

      val result = underTest.placeOrder(Order(basket, Address("unit", "test", "ave"))).execOrThrow()
      println(result)

      telemetry.calls.execOrThrow().foreach(println)
    }
  }
}
