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

case class Arc(center: Point, radius: Int, startAngle: Degrees, endAngle: Degrees) {
  def asSvg(arcWidth: Int, color: String, pathId: String, label: String) = {
    val startX = center.x + (radius * Math.cos(endAngle.asRadians))
    val startY = center.y + (radius * Math.sin(endAngle.asRadians))

    val endX = center.x + (radius * Math.cos(startAngle.asRadians))
    val endY = center.y + (radius * Math.sin(startAngle.asRadians))

    val largeArcFlag =
      if ((endAngle - startAngle).toInt % 360 > 180) then 1 else 0 // Large arc flag
    val sweepFlag = 0

    g(
      path(
        id     := pathId,
        d      := s"M $startX $startY A $radius $radius 0 $largeArcFlag $sweepFlag $endX $endY",
        stroke := color,
        strokeWidth := arcWidth,
        fill        := "none"
      ),
      text(fontSize := 20)(
        textPath(xLinkHref := s"#${pathId}", stags.attr("startOffset") := "50%")(label)
      )
    )
  }
}
