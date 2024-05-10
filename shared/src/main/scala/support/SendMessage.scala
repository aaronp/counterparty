package support

import scala.concurrent.duration.Duration
import scala.concurrent.duration.{given, *}

/** Representation of a message being sent from one actor to another
  */
case class SendMessage(
    from: Actor,
    to: Actor,
    timestamp: Long,
    duration: Duration,
    message: ujson.Value
) {
  def endTimestamp = timestamp + duration.toMillis

  def isActiveAt(time: Long) = timestamp <= time && time <= timestamp + duration.toMillis
}

object SendMessage:
  def test = SendMessage(
    support.Actor.person("foo", "dave"),
    support.Actor.person("bar", "carl"),
    1234,
    10.seconds,
    ujson.Obj("hello" -> "world")
  )
