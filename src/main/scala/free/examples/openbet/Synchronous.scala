package free.examples.openbet

import free.~>

import scala.util.Try


sealed trait HttpRequest

case class Get(url: String) extends HttpRequest

case class Post(url: String, body: String) extends HttpRequest

case class HttpResponse(code: Int, body: String)


/**
 * This is a "synchronous" interpreter, using [[Try]] as our Monad
 */
case class Synchronous(flags: FeatureFlags, httpClient: HttpRequest => HttpResponse) extends ~>[Operation, Try] :
  def apply[A](operation: Operation[A]): Try[A] =
    operation match {
      case GetFeatureFlags => Try(flags)
      case GetCustomerById(customerId) => Try {
        val response = httpClient(Get("/openBet"))
        UserData(customerId, response.body.contains("selfExcluded"))
      }
      case CheckGamStop(customerId) => Try {
        val response = httpClient(Get("/gamstop"))
        if response.code == 200 then Some(UserData(customerId, response.body.contains("selfExcluded"))) else None
      }
      case WriteSelfExclusion(cust, flag) => Try {
        Try(println(s"Writing self exclusion for $cust -> $flag"))
        val response = httpClient(Post(s"/gamstop/$cust", flag.toString))
        require(response.code == 200)
      }
      case Log(message) => Try(println(message))
    }