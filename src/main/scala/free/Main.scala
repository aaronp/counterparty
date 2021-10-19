package free


import zio.*

@main def showMeMyCode =
  val result: Free[MyApp, SearchResult] = MyApp.runUserQuery("demo")
  println("Free Structure: " + result)

object ZioApp extends zio.App {

  def run(args: List[String]) =
    import Interop.ziverge.{given, *}

    // our ad-hoc configuration assembled from ... stuff
    object config:
      val hint = "some user hint"
      val url = args.headOption.getOrElse("localhost:8080")
      given cnsol : Has[Console] = environment
      given nat : ~>[MyApp, Task] = live(url)

    import config.{given, *}
    val appAsTask : Task[SearchResult] = MyApp.runUserQuery(hint).foldMap[Task]

    val app = for {
      result <- appAsTask
      _ <- Console.printLine(s"GOT: " + result)
    } yield ()

    app.exitCode
}