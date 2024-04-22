package contract.interactive

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import org.scalajs.dom
import scalatags.Text

// import scalatags.JsDom.all.*
import scalatags.Text.{svgTags => stags}
import scalatags.Text.svgTags.*
import scalatags.Text.svgAttrs.*
import scalatags.Text.implicits.{given, *}

import scala.scalajs.js.annotation.*
import scala.concurrent.duration.{given, *}

class LayoutPage(
    actors: Set[Actor],
    messages: List[SendMessage],
    config: LayoutPage.Config = LayoutPage.Config.default()
):

  import LayoutPage.*

  lazy val sortedActors = actors.toList.sortBy(_.category)

  def svgGraph = svg(
    width  := config.width,
    height := config.height,
    xmlns  := "http://www.w3.org/2000/svg",
    style  := config.svgStyle
  )(
    Arc(config.center, 120, 20.degrees, 180.degrees).asSvg(30, "red"),
    Arc(config.center, 120, 180.degrees, 360.degrees).asSvg(30, "blue")
  ) // draw the circle

  private def actorPoints: Seq[Text.TypedTag[String]] = {
    val points = pointsAroundCenterpoint(config.center, config.radius, actors.size)

    points.zip(sortedActors).map { (point, actor) =>
      val (x, y)  = point
      val yOffset = y + config.actorConfig.labelYOffset

      g(
        stags.text(
          fill             := "white",
          stroke           := "black",
          textAnchor       := "middle",
          dominantBaseline := "middle",
          transform        := s"translate($x,$y),scale(${config.actorConfig.iconScale})"
        )(actor.`type`.icon),
        stags.text(
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

  extension (x: Double) {
    def degrees = Degrees(x)
  }

  opaque type Degrees = Double
  object Degrees:
    def apply(d: Double): Degrees = d
    extension (d: Degrees) {
      def asRadians: Double = (d * Math.PI / 180)
    }
  end Degrees
  import Degrees.*

  case class Arc(center: Point, radius: Int, startAngle: Degrees, endAngle: Degrees) {
    def asSvg(arcWidth: Int, color: String) = {

      // Convert degrees to radians

      // Calculate the start point
      val startX = center.x + (radius * Math.cos(endAngle.asRadians))
      val startY = center.y + (radius * Math.sin(endAngle.asRadians))

      // Calculate the end point
      val endX = center.x + (radius * Math.cos(startAngle.asRadians))
      val endY = center.y + (radius * Math.sin(startAngle.asRadians))

      // Define the SVG path command with arc
      val largeArcFlag = if ((endAngle - startAngle) % 360 > 180) then 1 else 0 // Large arc flag
      val sweepFlag    = 0

      g(
        path(
          d      := s"M $startX $startY A $radius $radius 0 $largeArcFlag $sweepFlag $endX $endY",
          stroke := color,
          strokeWidth := arcWidth,
          fill        := "none"
        )
      )
    }
    // def asSvg(arcWidth: Int, color: String = "black") = scalatags.Text.svgTags.path(
    //   fill        := "none",
    //   stroke      := color,
    //   strokeWidth := arcWidth
    // )(
    //   g(
    //     svgTags.marker()
    //   )
    // )
  }

  case class ActorConfig(labelYOffset: Int = 40, estimatedTextHeight: Int = 20, iconScale: Int = 4):
    def fullHeight = labelYOffset + estimatedTextHeight
  end ActorConfig

  case class Config(
      width: Int,
      height: Int,
      padding: Int,
      svgStyle: String = "background-color: white;",
      actorConfig: ActorConfig = ActorConfig()
  ):
    def fullHeight = height + padding + actorConfig.fullHeight
    def center     = Point(width / 2, height / 2)
    def radius     = (width.min(height) - padding - actorConfig.fullHeight) / 2

  object Config:
    def docWidth          = dom.window.innerWidth.toInt
    def docHeight         = dom.window.innerHeight.toInt
    def default(): Config = new Config(docWidth, docHeight, 300)
  end Config

  /** given a center point, lay out N points around it in a circle
    */
  private def pointsAroundCenterpoint(center: Point, radius: Int, n: Int): List[(Int, Int)] = {
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
