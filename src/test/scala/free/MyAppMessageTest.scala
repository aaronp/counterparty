package free

import _root_.free.examples.basic.{MyAppMessage, ReadUserQuery, RunSearch, SearchResult}

import org.scalatest.*
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MyAppMessageTest extends AnyWordSpec with Matchers {

  "MyApp.runUserQuery" should {
    "execute the search with the given user query" in {

      given testTransform: ~>[MyAppMessage, List] with
        def apply[A](app: MyAppMessage[A]): List[A] = app match {
          case ReadUserQuery(hint) => List(hint)
          case RunSearch(query, limit) => List(SearchResult(List(query, limit.toString)))
        }

      MyAppMessage.runUserQuery("test").foldMap[List] shouldBe List(SearchResult(List("test", "10")))
    }
  }
}