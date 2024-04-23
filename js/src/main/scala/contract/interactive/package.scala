package contract

import concurrent.duration.{given, *}
import ujson.*
import java.awt.Event
import java.util.EventListener

package object interactive {

  type Json = Value

  enum ActorType:
    case Person, Database, Queue, Email, Service

    def icon = this match
      case Person   => "👤"
      case Database => "🗄️"
      case Queue    => "📤"
      case Email    => "📧"
      case Service  => "🖥️"

  /** This is to represent the actors in a system. The people and things which send messsages and
    * data to each other
    *
    * @param type
    *   what kind of thing is this?
    * @param category
    *   what group does this belong to? (e.g. a suite of services)
    * @param label
    *   what should we call this thing?
    */
  case class Actor(`type`: ActorType, category: String, label: String)
  object Actor:
    def person(category: String, label: String)   = Actor(ActorType.Person, category, label)
    def database(category: String, label: String) = Actor(ActorType.Database, category, label)
    def queue(category: String, label: String)    = Actor(ActorType.Queue, category, label)
    def email(category: String, label: String)    = Actor(ActorType.Email, category, label)
    def service(category: String, label: String)  = Actor(ActorType.Service, category, label)

    /** Representation of a message being sent from one actor to another
      */
  case class SendMessage(
      from: Actor,
      to: Actor,
      timestamp: Long,
      duration: Duration,
      message: Json
  ) {
    def endTimestamp           = timestamp + duration.toMillis
    def isActiveAt(time: Long) = timestamp <= time && time <= timestamp + duration.toMillis
  }
  object SendMessage:
    def test = SendMessage(
      Actor.person("foo", "dave"),
      Actor.person("bar", "carl"),
      1234,
      10.seconds,
      ujson.Obj("hello" -> "world")
    )

    /** This keeps track of all the messages sent between actors in a system
      *
      * @param messages
      *   all the messages ever sent, in increasing timestamp order
      * @param actors
      *   all the actors who have ever sent or received a message
      * @param earliestTimestamp
      *   the timestamp of the first message
      * @param latestTimestamp
      *   the timestamp of the last message
      */
    case class EventLog private (
        messages: Vector[SendMessage],
        actors: Set[Actor],
        earliestTimestamp: Long,
        latestTimestamp: Long
    ):
      def add(msg: SendMessage): EventLog = {
        val newActors = actors + msg.from + msg.to
        // logic to ensure ordering
        if (messages.isEmpty) then
          copy(
            messages = Vector(msg),
            actors = newActors,
            earliestTimestamp = msg.timestamp,
            latestTimestamp = msg.timestamp
          )
        else if (msg.timestamp <= earliestTimestamp) then
          copy(messages = msg +: messages, actors = newActors, earliestTimestamp = msg.timestamp)
        else if (msg.timestamp >= latestTimestamp) then
          copy(messages = messages :+ msg, actors = newActors, latestTimestamp = msg.timestamp)
        else {
          val (before, after) = messages.partition(_.timestamp < msg.timestamp)
          copy(messages = before ++ Vector(msg) ++ after, actors = newActors)
        }
      }

      object EventLog:
        def apply(first: SendMessage, theRest: SendMessage*): EventLog = apply(first +: theRest)

        def apply(messages: Seq[SendMessage] = Nil): EventLog =
          messages.foldLeft(new EventLog(Vector.empty, Set.empty, 0, 0)) { case (log, msg) =>
            log.add(msg)
          }
}
