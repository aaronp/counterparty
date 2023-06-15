package free.examples.basic

import free.*
import zio.*

import scala.util.Try

case class SearchResult(hits : List[String]):
  override def toString: String = hits.mkString("SearchResult [\n", "\n", "\n]")

sealed trait MyAppMessage[A]
object MyAppMessage:
  def runUserQuery(hint : String): Free[MyAppMessage, SearchResult] = for {
    query <- ReadUserQuery(hint).freeM
    results <- RunSearch(query, 10).freeM
  } yield results

case class ReadUserQuery(hint : String) extends MyAppMessage[String]
case class RunSearch(query : String, limit : Int) extends MyAppMessage[SearchResult]
