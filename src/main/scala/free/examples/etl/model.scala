package free.examples.etl


import java.time.*
import scala.concurrent.duration.FiniteDuration
import scala.util.*


/**
 * Information about our application's settings and environment
 * @param debounceTimeout if we receive two events for the same upstream message within this amount of time, we filter out the second message
 * @param messageTimeout if the time from the original event and the current time exceeds this timeout then message processing should stop
 * @param now what the current timestamp is
 */
case class Config(debounceTimeout : FiniteDuration, messageTimeout : FiniteDuration, now : ZonedDateTime)

case class UpstreamMessage(messageId :String, userId :UserId, timestamp : ZonedDateTime) {
  def toJson : String = s"""{"messageId":"${messageId}", "userId":"${userId}", "timestamp":${timestamp.toInstant.toEpochMilli} }"""
}

object UpstreamMessage {
  def fromJson(json : String) : Try[UpstreamMessage] = json match {
    case s"""{"messageId":"${messageId}", "userId":"${userId}", "timestamp":${epochMilli} }""" =>
      val timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli.toLong), ZoneId.of("UTC"))
      Success(UpstreamMessage(messageId, userId, timestamp))
    case other => Failure(new Exception(s"Error parsing json '$json': $other"))
  }
}
case class EnrichedMessage(userName : String, originalMessage : UpstreamMessage) {
  def userId = originalMessage.userId
}

enum Operations[A]:
  // represents the ability to read our settings from the environment
  case GetConfig() extends Operations[Config]
  // represents the ability to parse a message from a byte array
  case ParseMessage(bytes : Array[Byte]) extends Operations[Try[UpstreamMessage]]
  // represents the ability to look-up a user
  case FindUser(userId: UserId) extends Operations[Option[String]]
  // represents the ability to store the fact that we've processed this message
  case SaveEvent(message: EnrichedMessage, now : ZonedDateTime) extends Operations[Unit]
  // represents an option to check what the previous time was for the event, which may or may not exist
  case ReadPreviousEvent(msg : EnrichedMessage) extends Operations[Option[ZonedDateTime]]
  // represents the ability to log/report an error
  case Log(message :String, isError : Boolean = false) extends Operations[Unit]
  // represents the ability to enqueue a message downstream
  case EnqueueMessage(message : EnrichedMessage) extends Operations[Unit]