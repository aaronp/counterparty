package contract.js.interactive

package object ui {

  export _root_.support.Colors
  export _root_.support.Actor
  export _root_.support.ActorType

  case class Point(x: Int, y: Int)

  opaque type Degrees = Double
  object Degrees:
    def apply(d: Double): Degrees = d
    extension (d: Degrees) {
      def asRadians: Double          = (d * Math.PI / 180)
      def -(other: Degrees): Degrees = Degrees(d - other)
      def +(other: Degrees): Degrees = Degrees(d + other)
      def *(other: Double): Degrees  = Degrees(d * other)
      def /(other: Double): Degrees  = Degrees(d / other)
      def toDouble: Double           = d
      def toInt: Int                 = d.toInt
    }
  end Degrees
  import Degrees.*

  extension (x: Double) {
    def degrees = Degrees(x)
  }

  extension (x: Int) {
    def degrees = Degrees(x)
  }
}
