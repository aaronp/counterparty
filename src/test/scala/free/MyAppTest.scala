package free

import org.scalatest.*
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MyAppTest extends AnyWordSpec with Matchers {

  "MyApp.runUserQuery" should {
    "execute the search with the given user query" in {
      given Monad[List] with
        override def pure[A](a: A) = List(a)

        override def flatMap[A, B](fa: List[A], f: A => List[B]) = fa.flatMap(f)

      given testTransform: ~>[MyApp, List] with
        def apply[A](app: MyApp[A]): List[A] = app match {
          case ReadUserQuery(hint) => List(hint)
          case RunSearch(query, limit) => List(SearchResult(List(query, limit.toString)))
        }

      MyApp.runUserQuery("test").foldMap[List] shouldBe List(SearchResult(List("test", "10")))
    }
  }
}