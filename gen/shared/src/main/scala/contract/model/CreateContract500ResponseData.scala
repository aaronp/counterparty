/** Contract Management API API for managing draft contracts
  *
  * OpenAPI spec version: 1.0.0
  *
  * Contact: team@openapitools.org
  *
  * NOTE: This class is auto generated by OpenAPI Generator.
  *
  * https://openapi-generator.tech
  */

// this model was generated using modelData.mustache
package contract.model
import scala.util.control.NonFatal
import scala.util.*

// see https://com-lihaoyi.github.io/upickle/
import upickle.default.{ReadWriter => RW, macroRW}
import upickle.default.*

/** CreateContract500ResponseData a data transfer object, primarily for simple json serialisation.
  * It has no validation - there may be nulls, values out of range, etc
  */
case class CreateContract500ResponseData(
    /* A message detailing an internal error that prevented the contract creation. */
    error: String = ""
) {

  def asJson: String = write(this)

  def validationErrors(path: Seq[Field], failFast: Boolean): Seq[ValidationError] = {
    val errors = scala.collection.mutable.ListBuffer[ValidationError]()
    // ==================
    // error

    errors.toSeq
  }

  def validated(failFast: Boolean = false): scala.util.Try[CreateContract500Response] = {
    validationErrors(Vector(), failFast) match {
      case Seq()            => Success(asModel)
      case first +: theRest => Failure(ValidationErrors(first, theRest))
    }
  }

  /** use 'validated' to check validation */
  def asModel: CreateContract500Response = {
    CreateContract500Response(
      error = Option(
        error
      )
    )
  }
}

object CreateContract500ResponseData {

  given readWriter: RW[CreateContract500ResponseData] = macroRW

  def fromJsonString(jason: String): CreateContract500ResponseData = try {
    read[CreateContract500ResponseData](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason': $e")
  }

  def manyFromJsonString(jason: String): Seq[CreateContract500ResponseData] = try {
    read[List[CreateContract500ResponseData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as list: $e")
  }

  def manyFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Seq[CreateContract500Response]] = {
    Try(manyFromJsonString(jason)).flatMap { list =>
      list.zipWithIndex.foldLeft(Try(Vector[CreateContract500Response]())) {
        case (Success(list), (next, i)) =>
          next.validated(failFast) match {
            case Success(ok) => Success(list :+ ok)
            case Failure(err) =>
              Failure(new Exception(s"Validation error on element $i: ${err.getMessage}", err))
          }
        case (fail, _) => fail
      }
    }
  }

  def mapFromJsonString(jason: String): Map[String, CreateContract500ResponseData] = try {
    read[Map[String, CreateContract500ResponseData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as map: $e")
  }

  def mapFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Map[String, CreateContract500Response]] = {
    Try(mapFromJsonString(jason)).flatMap { map =>
      map.foldLeft(Try(Map[String, CreateContract500Response]())) {
        case (Success(map), (key, next)) =>
          next.validated(failFast) match {
            case Success(ok) => Success(map.updated(key, ok))
            case Failure(err) =>
              Failure(new Exception(s"Validation error on element $key: ${err.getMessage}", err))
          }
        case (fail, _) => fail
      }
    }
  }
}