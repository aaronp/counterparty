package contract.interactive.ui

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

/** Represents the data needed to render a category
  */
case class CategorySection(category: String, i: Int, totalCategories: Int, color: String) {

  // the background and label arcs
  def arcs(config: Config) = {
    // the start and end angles for the arc
    val start = (i * 360 / totalCategories).degrees
    val end   = ((i + 1) * 360 / totalCategories).degrees - config.actorConfig.categoryGap

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
        category
      )
    Seq(backgroundArc, labelArc)
  }
}
