package free.examples.etl.ui

import _root_.free.examples.{*, given}
import _root_.free.examples.etl.{Config, Logic, Operations, UpstreamMessage}
import free.*

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import scala.concurrent.duration.*
import scala.util.control.NonFatal

class TestInterpreter(db: InMemoryDB = InMemoryDB(),
                      config : Config = Config(1.seconds, 10.seconds, ZonedDateTime.of(LocalDateTime.of(1, 2, 3, 4, 5), ZoneId.of("UTC"))),
                      logicOverrides: PartialFunction[Operations[_], _] = PartialFunction.empty) {


  def testMessage(input: UpstreamMessage): Buffer = testBytes(input.toJson.getBytes("UTF8"))

  def testBytes(inputBytes: Array[Byte]): Buffer = {
    // this is our *real* logic/control flow for a message, returned in (free monad) tree structure
    val logic = Logic.onMessage(inputBytes)

    //we now "interpret" (a.k.a. evaluate) that logic into a "state monad" which just keeps the commands
    // in a test buffer (i.e. don't actually execute it, just
    // write down what commands would be issued given the input)
    val State(run) = logic.foldMap[BufferState]

    // we can now "run" that state monad using an initially empty buffer
    run(Buffer(Nil))._2
  }
  def withConfig(cfg : Config) = new TestInterpreter(db, cfg, logicOverrides)

    private val handler = (_: Operations[_]) match {
      case op if logicOverrides.isDefinedAt(op) => logicOverrides(op)

      // the default cases
      case Operations.GetConfig() => config
      case Operations.ParseMessage(bytes) =>
        val json = new String(bytes)
        UpstreamMessage.fromJson(json)
      case Operations.FindUser(userId) if userId.contains("missing") => None
      case Operations.FindUser(userId) => Some(s"username for $userId")
      case Operations.SaveEvent(msg, now) => db.update(msg.userId, now)
      case Operations.ReadPreviousEvent(msg) => db.get(msg.userId)
      case Operations.Log(msg, error) => println(s"logging($msg, $error)")
      case Operations.EnqueueMessage(message) => println(s"enqueuing $message")
    }

    private val interpreter = (op: Operations[_]) => {
      handler(op) match {
        case result => State.combine[Buffer, Any](Buffer(op, result), result)
      }
    }

    given ~>[Operations, BufferState] with

      def apply[A](op: Operations[A]): BufferState[A] =
        val result = interpreter(op.asInstanceOf[Operations[Any]])
        try {
          result.asInstanceOf[BufferState[A]]
        } catch {
          case NonFatal(e) =>
            println(s"$result wasn't A")
            println(e)
            throw e
        }

}
