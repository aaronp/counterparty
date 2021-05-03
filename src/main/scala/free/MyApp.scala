package free

import zio.{Console, Has}

import scala.util.Try

case class SearchResult(hits : List[String]):
  override def toString: String = hits.mkString("SearchResult [\n", "\n", "\n]")

sealed trait MyApp[A]
object MyApp:
  def runUserQuery(hint : String): Free[MyApp, SearchResult] = for {
    query <- Free.liftM(ReadUserQuery(hint))
    results <- Free.liftM(RunSearch(query, 10))
  } yield results

case class ReadUserQuery(hint : String) extends MyApp[String]
case class RunSearch(query : String, limit : Int) extends MyApp[SearchResult]
