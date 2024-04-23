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
import scala.collection.mutable.ListBuffer

object CategorySection {
  def forActors(actors: Set[Actor], config: Config): Seq[CategorySection] = {

    val actorsByCateogory: Map[String, Set[Actor]] = actors.groupBy(_.category)

    val colors = Colors(actorsByCateogory.size)

    // we're trying to build up the layout for each set of 'actors' in the system, with a background arc behind that category
    //
    // to do that, we first create the angle ranges, which is the percentage size of the whole circle per category
    // (that is, if one category has 3 actors and another has 6, the first will have 1/3 of the circle and the second 2/3)
    //
    // then we create the actual sections, which are the arcs that will be drawn behind the actors
    //
    // this really ugly fold is just to keep track of the start and end angles for each category as we build up the sections
    //
    // we start with 0 degrees and an empty list of sections, and then for each category we calculate the start and end angles
    val (_, categories) =
      actorsByCateogory.zip(colors).foldLeft(0.degrees -> Seq.empty[CategorySection]) {

        // start is the start angle for the current category, and sections is the list of sections we've built up so far
        //
        // category is just the the actorsByCateogory map entry, which is the name of the category, and actorsForCategory is the actors in that category
        //
        // we also have zipped together the categories with a color for that category
        case ((start, sections), ((category, actorsForCategory), color)) =>
          //
          // what percentage of the whole circle do these actors account for?
          val proportionalArcSize: Degrees = {
            val ratioOfThisCategory = actorsForCategory.size / actors.size.toDouble
            360.degrees * ratioOfThisCategory
          }

          // the end angle is the start angle plus the proportional arc size, minus the gap between categories
          val end            = start + proportionalArcSize - config.actorConfig.categoryGap
          val nextStartAngle = start + proportionalArcSize

          val newSection = CategorySection(
            category,
            actorsForCategory.toSeq.sortBy(_.label),
            color,
            start,
            end,
            config
          )

          (nextStartAngle, sections :+ newSection)
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
    arcStart: Degrees,
    arcEnd: Degrees,
    config: Config
) {
  require(arcEnd.toDouble > arcStart.toDouble)

  private def centerPointWithActor = actorsInThisCategory.zipWithIndex.flatMap { case (actor, i) =>
    // the gaps between the actors in this category are the angle between the start and end of the category divided by the number of actors
    val angleStepSize = (arcEnd - arcStart) / (actorsInThisCategory.size + 1)

    actorsInThisCategory
      .foldLeft(arcStart -> Seq.empty[(Point, Actor)]) { case ((angle, acc), actor) =>
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

  def actorComponents: Seq[TypedTag[String]] = {

    val steps         = actorsInThisCategory.size
    val step: Degrees = (arcEnd - arcStart) / (actorsInThisCategory.size + 1)

    actorsInThisCategory.zipWithIndex.map { case (actor, i) =>
      val offset = arcStart + (step * (i + 1))

      val x       = config.center.x + config.radius * Math.cos(offset.asRadians)
      val y       = config.center.y + config.radius * Math.sin(offset.asRadians)
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
  }

  def backgroundArcComponents: Seq[TypedTag[String]] = {

    val thickness = config.actorConfig.categoryThickness

    val backgroundArc = Arc(config.center, config.radius, arcStart, arcEnd)
      .asSvg(
        thickness,
        color,
        s"arc-${category.filter(_.isLetterOrDigit)}",
        ""
      )

    // draw a second arc for the label on the outside (e.g. radius + half the thickness + 20 pixels)
    val labelRadius = config.radius + thickness / 2 + config.actorConfig.estimatedTextHeight
    val labelArc = Arc(config.center, labelRadius, arcStart, arcEnd)
      .asSvg(
        1,
        "white",
        s"arclabel-${category.filter(_.isLetterOrDigit)}",
        f"$category from $arcStart%.2f to $arcEnd%.2f"
      )
    Seq(backgroundArc, labelArc)
  }
}
