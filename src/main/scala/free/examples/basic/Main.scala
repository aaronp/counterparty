package free.examples.basic

import free.*
import zio.*

@main def showMeMyCode =
  val result: Free[MyAppMessage, SearchResult] = MyAppMessage.runUserQuery("demo")
  println("Free Structure: " + result)

object ZioApp extends ZIOAppDefault {


  /**
   * This is our interpreter for this app, which provides the natural transformation (read : ~>)
   * of our 'MyAppMessage[_]' type onto the zio 'Task' effect
   *
   * @param url the url of an endpoint
   * @param hasConsole a 'HasConsole' environment used for
   * @return
   */
  def live(url: String)(using hasConsole: ZEnvironment[Console]) = new~>[MyAppMessage, Task] {
    def apply[A](app: MyAppMessage[A]): Task[A] = app match {
      case ReadUserQuery(hint) =>
        val io = Console.printLine(s"USER [$hint]") *> Console.readLine
        io.provideEnvironment(hasConsole).orDie
      case RunSearch(query, limit) => ZIO.attempt {
        // fake service
        val results = (0 until limit).map { i =>
          s"curl -H fake:$i -XPOST $url -d $query"
        }
        SearchResult(results.toList)
      }
    }
  }

//  def run(args: List[String]) =

  def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    // our ad-hoc configuration assembled from ... stuff
    case class Settings(url : String, hint: String = "some user hint"):
//      val url = args.headOption.getOrElse("localhost:8080")
      given cnsol : ZEnvironment[Console] = ZEnvironment(Console.ConsoleLive)
      given nat : ~>[MyAppMessage, Task] = live(url)

    def appAsTask(config :Settings) : Task[SearchResult] = {
      import config.{*, given}
      MyAppMessage.runUserQuery(hint).foldMap[Task]
    }

    val app = for {
      args <- getArgs
      config = Settings(url = args.headOption.getOrElse("localhost:8080"))
      result <- appAsTask(config)
      _ <- Console.printLine(s"GOT: " + result)
    } yield ()

    app.exitCode
}