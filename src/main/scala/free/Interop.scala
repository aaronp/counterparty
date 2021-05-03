package free

import zio.Task

import java.io.IOException


object Interop:

  object ziverge:

    given Monad[Task] with
      override def flatMap[A, B](fa: Task[A], f: A => Task[B]): Task[B] = fa.flatMap(f)
      override def pure[A](a: A): Task[A] = Task.succeed(a)

    import zio.*

    def live(url: String)(using hasConsole: Has[Console]) = new ~>[MyApp, Task] {
      def apply[A](app: MyApp[A]): Task[A] = app match {
        case ReadUserQuery(hint) =>
          val io = Console.printLine(s"USER [$hint]") *> Console.readLine
          io.provide(hasConsole).orDie
        case RunSearch(query, limit) => Task {
          // fake service
          val results = (0 until limit).map { i =>
            s"curl -H fake:$i -XPOST $url -d $query"
          }
          SearchResult(results.toList)
        }
      }
    }

  end ziverge
end Interop
