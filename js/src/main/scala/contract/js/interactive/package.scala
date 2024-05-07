package contract.js

import concurrent.duration.{given, *}
import ujson.*
import _root_.contract.*
import java.util.EventListener
import CreateDraftLogic.*

package object interactive {

  type Json = Value

  export _root_.support.Actor
  export _root_.support.ActorType

  /** Representation of a message being sent from one actor to another
    */
  case class SendMessage(
      from: support.Actor,
      to: support.Actor,
      timestamp: Long,
      duration: Duration,
      message: Json
  ) {
    def endTimestamp           = timestamp + duration.toMillis
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
        actors: Set[support.Actor],
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

  // convenience data structure for folding over our operations
  case class FoldData(messages: Seq[SendMessage]) {
    def latestTime = messages.map(_.endTimestamp).max
    // def offset(delta: Int)     = copy(timestamp = timestamp + delta)
    // def delta(other: FoldData) = (timestamp - other.timestamp).abs
    def asState[A](result: A): State[FoldData, A] =
      State.combine[FoldData, A](this, result)
  }
  object FoldData {
    def apply(first: SendMessage, messages: SendMessage*) = new FoldData(first +: messages.toSeq)
    given Semigroup[FoldData] with {
      // TODO - here where we combine, we need to offset all messages by some
      // delta so that they are all in the same time frame
      override def combine(x: FoldData, y: FoldData) =
        new FoldData(x.messages ++ y.messages)
    }
  }

  enum Category:
    case ContractSystem, CounterpartyA, CounterpartyB

  type FoldState[A] = State[FoldData, A]

}
