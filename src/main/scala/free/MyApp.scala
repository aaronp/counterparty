package free

import zio.{Console, Has}

import scala.util.Try

case class SearchResult(hits : List[String]):
  override def toString: String = hits.mkString("SearchResult [\n", "\n", "\n]")

sealed trait MyApp[A]
object MyApp:
  def runUserQuery(hint : String): Free[MyApp, SearchResult] = for {
    query <- ReadUserQuery(hint).freeM
    results <- RunSearch(query, 10).freeM
  } yield results

case class ReadUserQuery(hint : String) extends MyApp[String]
case class RunSearch(query : String, limit : Int) extends MyApp[SearchResult]
