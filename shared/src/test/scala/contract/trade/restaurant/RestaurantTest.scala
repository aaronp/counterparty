package contract.trade.restaurant

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import contract.*
import contract.trade.*
import zio.*

class RestaurantTest extends AnyWordSpec with Matchers {

  "Restaurant.placeOrder" should {
    "restock when we get low on stuff" in {
      given telemetry: Telemetry = Telemetry()

      val testData = new RestaurantTestData
      import testData.*

      calls.foreach(println)

      println("Mermaid:")
      println(telemetry.asMermaidDiagram().execOrThrow())

      result shouldBe OrderId("order-2")
    }
  }
}
