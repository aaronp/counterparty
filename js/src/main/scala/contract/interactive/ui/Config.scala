package contract.interactive.ui

import org.scalajs.dom
case class ActorConfig(
    // how much further down should the actor labels be from the actor icon?
    labelYOffset: Int = 40,
    // how big is the text size do we think (hack for laying out actors in a circle)
    estimatedTextHeight: Int = 20,
    // how much should we scale the emoji icons?
    iconScale: Int = 4,
    // how big of a gap should we have between categories
    categoryGap: Degrees = 5.degrees,
    // how thick is the category arch?
    categoryThickness: Int = 250
):
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
