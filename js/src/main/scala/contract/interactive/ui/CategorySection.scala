package contract.interactive.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import org.scalajs.dom
import scalatags.Text

import contract.interactive.*
import scalatags.Text.{svgTags => stags}
import scalatags.Text.svgTags.*
import scalatags.Text.svgAttrs.*
import scalatags.Text.implicits.{given, *}

import scala.scalajs.js.annotation.*
import scala.concurrent.duration.{given, *}
import scalatags.Text.TypedTag

object CategorySection {
  def forActors(actors: Set[Actor], config: Config) = {

    val actorsByCateogory: Map[String, Set[Actor]] = actors.groupBy(_.category)

    val colors = Colors(actorsByCateogory.size)

    // we're trying to build up the layout for each set of 'actors' in the system, with a background arc behind that category
    //
    // to do that, we first create the angle ranges, which is the percentage size of the whole circle per category

    val (_, categories) =
      actorsByCateogory.zipWithIndex.foldLeft(0.degrees -> Seq.empty[CategorySection]) {
        case ((start, sections), ((category, actorsForCategory), i)) =>
          val percentSize = 360 * actorsForCategory.size / actors.size.toDouble

          val end = start + percentSize.degrees - config.actorConfig.categoryGap
          (
            start + percentSize.degrees,
            sections :+ CategorySection(
              category,
              actorsForCategory.toSeq.sortBy(_.label),
              colors(i),
              start,
              end,
              config
            )
          )
      }

    categories
  }

}

/** Represents the data needed to render a category
  */
case class CategorySection(
    category: String,
    actorsInThisCategory: Seq[Actor],
    color: String,
    start: Degrees,
    end: Degrees,
    config: Config
) {
  require(end.toDouble > start.toDouble)

  val centerPointWithActor = actorsInThisCategory.zipWithIndex.flatMap { case (actor, i) =>
    val angleStepSize = (end - start) / (actorsInThisCategory.size + 1)

    actorsInThisCategory
      .foldLeft(start -> Seq.empty[(Point, Actor)]) { case ((angle, acc), actor) =>
        val next = angle + angleStepSize
        val x    = config.center.x + config.radius * Math.cos(next.asRadians)
        val y    = config.center.y + config.radius * Math.sin(next.asRadians)
        val tuple: Seq[(Point, Actor)] =
          acc :+ (Point(x.toInt, y.toInt), actor
            .copy(label = s"$i: ${actor.label} at $next"))
        (next + angleStepSize, tuple)
      }
      ._2
  }

  def actorComponents = centerPointWithActor.map { case (point, actor) =>
    val (x, y)  = (point.x, point.y)
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
      )(s"${actor.label}")
    )
  }

  def backgroundArcComponents: Seq[TypedTag[String]] = {

    val thickness = config.actorConfig.categoryThickness

    val backgroundArc = Arc(config.center, config.radius, start, end)
      .asSvg(
        thickness,
        color,
        s"arc-${category.filter(_.isLetterOrDigit)}",
        ""
      )

    val labelRadius = config.radius + thickness / 2 + 20
    val labelArc = Arc(config.center, labelRadius, start, end)
      .asSvg(
        1,
        "white",
        s"arclabel-${category.filter(_.isLetterOrDigit)}",
        s"$category from $start to $end"
      )
    Seq(backgroundArc, labelArc)
  }
}
