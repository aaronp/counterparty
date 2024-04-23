package contract.interactive.ui

import contract.interactive.*
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
import scala.collection.MapView
import scalatags.Text.TypedTag

class LayoutPage(
    actors: Set[Actor],
    messages: List[SendMessage],
    config: Config = Config.default()
):

  import LayoutPage.*

  lazy val sortedActors = actors.toList.sortBy(_.category)

  def svgGraph = svg(
    width  := config.width,
    height := config.height,
    xmlns  := "http://www.w3.org/2000/svg",
    style  := config.svgStyle
  ) {
    val sections = CategorySection.forActors(actors, config)
    sections.flatMap(_.backgroundArcComponents) ++ sections.flatMap(_.actorComponents)
  }

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
    Actor.person("counterparty-A", "Alice's Lawyer"),
    Actor.database("counterparty-service", "MongoDB"),
    Actor.queue("counterparty-service", "Notification Queue"),
    Actor.service("counterparty-service", "Counterparty Service"),
    Actor.service("counterparty-service", "Notification ETL"),
    Actor.email("counterparty-service", "Email Service"),
    Actor.person("counterparty-B", "Bob")
  )
  val List(alice, lawyer, mongo, queue, service, etl, email, bob) = actors
  val messages = List(
    SendMessage(alice, service, 1234, 1.seconds, ujson.Obj("hello" -> "world")),
    SendMessage(service, mongo, 2345, 2.seconds, ujson.Obj("hello" -> "world")),
    SendMessage(alice, service, 1234, 10.seconds, ujson.Obj("hello" -> "world"))
  )
  LayoutPage(actors.toSet, messages).render
  // dom.document.querySelector("#app").innerHTML = LayoutPage.svgGraph.render
}
