package contract.interactive

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

// import scalatags.JsDom.all.*
import scalatags.Text.svgTags.*
import scalatags.Text.svgAttrs.*
import scalatags.Text.implicits.{given, *}

import scala.scalajs.js.annotation.*
import scala.concurrent.duration.{given, *}

class LayoutPage(
    actors: Set[Actor],
    messages: List[SendMessage],
    config: LayoutPage.Config = LayoutPage.Config.forSize()
):

  import LayoutPage.*

  lazy val sortedActors = actors.toList.sortBy(_.category)

  def svgGraph = svg(
    width  := config.width,
    height := config.height,
    xmlns  := "http://www.w3.org/2000/svg",
    style  := config.svgStyle
  ) {
    val points = pointsAroundCenterpoint(config.center, config.radius, actors.size)

    points.zip(sortedActors).map { (point, actor) =>
      val (x, y)  = point
      val yOffset = y + config.actorConfig.labelYOffset

      g(
        scalatags.Text.svgTags.text(
          fill             := "white",
          stroke           := "black",
          textAnchor       := "middle",
          dominantBaseline := "middle",
          transform        := s"translate($x,$y),scale(3)"
        )(actor.`type`.icon),
        scalatags.Text.svgTags.text(
          textAnchor       := "middle",
          dominantBaseline := "middle",
          transform        := s"translate($x,$yOffset)"
        )(actor.label)
      )
    }

  }

  def render = dom.document.querySelector("#app").innerHTML = svgGraph.render

end LayoutPage

object LayoutPage {

  case class Point(x: Int, y: Int)

  case class ActorConfig(labelYOffset: Int = 40, estimatedTextHeight: Int = 20):
    def fullHeight = labelYOffset + estimatedTextHeight
  end ActorConfig

  case class Config(
      width: Int,
      height: Int,
      padding: Int,
      svgStyle: String = "background-color: yellow;",
      actorConfig: ActorConfig = ActorConfig()
  ):
    def fullHeight = height + padding + actorConfig.fullHeight
    def center     = Point(width / 2, height / 2 - padding - actorConfig.fullHeight)
    def radius     = Math.min(width, height) / 2 - padding - actorConfig.fullHeight

  object Config:
    def docWidth          = dom.window.innerWidth.toInt
    def docHeight         = dom.window.innerHeight.toInt
    def forSize(): Config = new Config(docWidth, docHeight, 20)
  end Config

  /** given a center point, lay out N points around it in a circle
    */
  def pointsAroundCenterpoint(center: Point, radius: Int, n: Int): List[(Int, Int)] = {
    import center._
    val angle = 2 * Math.PI / n
    (0 until n)
      .map(i => {
        val newX = x + radius * Math.cos(i * angle)
        val newY = y + radius * Math.sin(i * angle)
        (newX.toInt, newY.toInt)
      })
      .toList
  }

  def animageMessage(from: Point, to: Point, duration: FiniteDuration) = {
    circle(cx := from.x, cy := from.y, r := 5, fill := "red")
  }

}

@main
def layout(): Unit = {
  val actors = List(
    Actor.person("counterparty-A", "Alice"),
    Actor.database("counterparty-service", "MongoDB"),
    Actor.queue("counterparty-service", "Notification Queue"),
    Actor.service("counterparty-service", "Counterparty Service"),
    Actor.service("counterparty-service", "Notification ETL"),
    Actor.email("counterparty-service", "Email Service"),
    Actor.person("counterparty-B", "Bob")
  )
  val List(alice, mongo, queue, service, etl, email, bob) = actors
  val messages = List(
    SendMessage(alice, service, 1234, 1.seconds, ujson.Obj("hello" -> "world")),
    SendMessage(service, mongo, 2345, 2.seconds, ujson.Obj("hello" -> "world")),
    SendMessage(alice, service, 1234, 10.seconds, ujson.Obj("hello" -> "world"))
  )
  LayoutPage(actors.toSet, messages).render
  // dom.document.querySelector("#app").innerHTML = LayoutPage.svgGraph.render
}
