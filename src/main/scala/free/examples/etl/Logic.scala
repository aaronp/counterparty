package free.examples.etl

import java.time.{Duration, ZonedDateTime}
import scala.concurrent.duration.FiniteDuration
import scala.util.*

/**
 * Houses our ETL business logic.
 *
 * We debounce messages if we see too many (according to a configuration), and enrich them by looking up user names for user ids.
 */
object Logic {

  import Operations.*
  import free.{*, given}


  /**
   * @param bytes the input message bytes
   * @return our message-handling app logic (as a free monad data structure)
   */
  def onMessage(bytes: Array[Byte]): Free[Operations, Unit] = for {
    _ <- Log("handling message").freeM
    messageResult <- ParseMessage(bytes).freeM
    result <- handleParsedMessage(messageResult)
  } yield result

  private def handleParsedMessage(message: Try[UpstreamMessage]) = message match {
    case Success(msg) =>
      for {
        config: Config <- GetConfig().freeM
        result <- if config.now.isBefore(msg.timestamp) then {
          log(s"ignoring message for user ${msg.userId} seemingly sent in the future. now=${config.now} and msg.timestamp is ${msg.timestamp}")
        } else enrichMessage(config, msg)
      } yield result
    case Failure(err) => error(s"failed to parse message: ${err}")
  }

  private def enrichMessage(config: Config, message: UpstreamMessage) = {
    for {
      userOpt: Option[String] <- FindUser(message.userId).freeM
      result <- userOpt match {
        case Some(user) => processEnrichedMessage(config, EnrichedMessage(user, message))
        case None => log(s"user ${message.userId} not found")
      }
    } yield result
  }

  private def processEnrichedMessage(config: Config, message: EnrichedMessage) = {

    val msgTime = message.originalMessage.timestamp
    val totalElapsed = durationOf(config.now, msgTime)

    if totalElapsed > config.messageTimeout then {
      log(s"Filtering really old message ${message.originalMessage.messageId} as ${totalElapsed.toSeconds}s passed and messageTimeout is ${config.messageTimeout}")
    } else {
      log(s"Message '${message.originalMessage.messageId}' handled as ${totalElapsed.toSeconds}s is less than the config timeout of ${config.messageTimeout}").flatMap { _ =>
        ReadPreviousEvent(message).freeM.flatMap {
          // no previously saved message - nothing to check
          case None =>
            for {
              _ <- log("no previous message time found")
              result <- pushMessage(config, message)
            } yield result
          case Some(lastProcessedTime: ZonedDateTime) =>
            val lastProcessedElapsed = durationOf(lastProcessedTime, message.originalMessage.timestamp)
            if lastProcessedElapsed <= config.debounceTimeout then {
              log(s"Debounced message ${message.originalMessage.messageId} as ${lastProcessedElapsed.toSeconds}s passed (<= ${config.debounceTimeout})since the last message was sent at $lastProcessedTime")
            } else {
              for {
                _ <- log(s"Last message was received at $lastProcessedTime, and ${lastProcessedElapsed.toSeconds}s is > ${config.debounceTimeout} timeout")
                result <- pushMessage(config, message)
              } yield result
            }
        }
      }

    }
  }

  private def pushMessage(config: Config, message: EnrichedMessage) = {
    for {
      _ <- SaveEvent(message, config.now).freeM
      _ <- log(s"saved ${message.originalMessage.messageId}")
      _ <- EnqueueMessage(message).freeM
      _ <- log(s"enriched ${message.originalMessage.messageId}")
    } yield ()
  }
}
