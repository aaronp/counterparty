package contract.trade.market

import contract.{*, given}
import contract.trade.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import zio.*
import support.*

import scala.concurrent.duration.*

class MarketplaceTest extends AnyWordSpec with Matchers {

  import MarketplaceLogic.*

  "Marketplace" should {
    "trace orders" in {

      given telemetry: Telemetry = Telemetry()
      val testData               = new MarketplaceTestData
      import testData.*

      println(result)
      telemetry.calls.execOrThrow().foreach(println)
    }
  }
}
