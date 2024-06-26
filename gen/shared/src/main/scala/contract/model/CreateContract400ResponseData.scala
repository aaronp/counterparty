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

/** CreateContract400ResponseData a data transfer object, primarily for simple json serialisation.
  * It has no validation - there may be nulls, values out of range, etc
  */
case class CreateContract400ResponseData(
    /* A message detailing the error with the provided input. */
    error: String = ""
) {

  def asJson: String = write(this)

  def validationErrors(path: Seq[Field], failFast: Boolean): Seq[ValidationError] = {
    val errors = scala.collection.mutable.ListBuffer[ValidationError]()
    // ==================
    // error

    errors.toSeq
  }

  def validated(failFast: Boolean = false): scala.util.Try[CreateContract400Response] = {
    validationErrors(Vector(), failFast) match {
      case Seq()            => Success(asModel)
      case first +: theRest => Failure(ValidationErrors(first, theRest))
    }
  }

  /** use 'validated' to check validation */
  def asModel: CreateContract400Response = {
    CreateContract400Response(
      error = Option(
        error
      )
    )
  }
}

object CreateContract400ResponseData {

  given readWriter: RW[CreateContract400ResponseData] = macroRW

  def fromJsonString(jason: String): CreateContract400ResponseData = try {
    read[CreateContract400ResponseData](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason': $e")
  }

  def manyFromJsonString(jason: String): Seq[CreateContract400ResponseData] = try {
    read[List[CreateContract400ResponseData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as list: $e")
  }

  def manyFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Seq[CreateContract400Response]] = {
    Try(manyFromJsonString(jason)).flatMap { list =>
      list.zipWithIndex.foldLeft(Try(Vector[CreateContract400Response]())) {
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

  def mapFromJsonString(jason: String): Map[String, CreateContract400ResponseData] = try {
    read[Map[String, CreateContract400ResponseData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as map: $e")
  }

  def mapFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Map[String, CreateContract400Response]] = {
    Try(mapFromJsonString(jason)).flatMap { map =>
      map.foldLeft(Try(Map[String, CreateContract400Response]())) {
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
