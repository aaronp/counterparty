package free.examples

import free.examples.etl.Operations.Log

import java.time.{Duration, ZonedDateTime}
import scala.concurrent.duration.FiniteDuration
import free.freeM

package object etl {

  type UserId = String
//
//  def log(msg: String) = Log(msg).freeM

  def error(msg: String) = Log(msg, true).freeM

  def durationOf(a: ZonedDateTime, b: ZonedDateTime): FiniteDuration = {
    import scala.jdk.DurationConverters.{*, given}
    Duration.between(a.toInstant, b.toInstant).toScala
  }

}
