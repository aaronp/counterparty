package contract.trade

import contract.*
import contract.trade.restaurant.*
import contract.trade.market.*
import eie.io.{given, *}
import scala.language.implicitConversions

case class Scenario(title: String, input: String, mermaid: String) {
  def mermaidIndented = mermaid.linesIterator.mkString("\n\t|", "\n\t|", "\n")
  def asDoc = s"""
                 |## $title
                 |
                 |When given:
                 |```scala
                 |$input
                 |```
                 |
                 |This is what will happen:    
                 |$mermaidIndented
   """.stripMargin('|')
}

def restaurantFlow = {
  given telemetry: Telemetry = Telemetry()

  val testData = new RestaurantTestData
  import testData.*

  testData.result.ensuring(_ != null) // <-- we have to evaluate this / run

  val mermaid = telemetry.asMermaidDiagram().execOrThrow()

  Scenario("Restaurant", testData.order.toString(), mermaid)
}

def marketFlow = {
  given telemetry: Telemetry = Telemetry()

  val testData = new MarketplaceTestData
  import testData.*

  testData.result.ensuring(_ != null) // <-- we have to evaluate this / run
  telemetry.calls.execOrThrow().foreach(println)

  val mermaid = telemetry.asMermaidDiagram().execOrThrow()
  Scenario("Marketplace", input.toString, mermaid)
}

@main def genDocs() = {

  val scenarios = List(
    restaurantFlow,
    marketFlow
  )

  val header = """# Scenarios
                 |This file was generated using [GenDocs](../jrm/src/test/scala/mermaid/GenDocs.scala)
                 |
                 |To regenerate, run:
                 |```sh
                 |sbt test:run
                 |```
                 |""".stripMargin('|')

  val content = scenarios.map(_.asDoc).mkString(header, "\n", "")

  "./docs/scenarios.md".asPath.text = content
}
