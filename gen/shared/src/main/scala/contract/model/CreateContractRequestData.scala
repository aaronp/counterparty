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

/** CreateContractRequestData a data transfer object, primarily for simple json serialisation. It
  * has no validation - there may be nulls, values out of range, etc
  */
case class CreateContractRequestData(
    /* The name of the first counterparty in the contract. */
    firstCounterparty: String,

    /* The name of the second counterparty in the contract. */
    secondCounterparty: String,

    /* The terms agreed upon in the contract. */
    terms: String,

    /* The conditions of the contract. */
    conditions: String
) {

  def asJson: String = write(this)

  def validationErrors(path: Seq[Field], failFast: Boolean): Seq[ValidationError] = {
    val errors = scala.collection.mutable.ListBuffer[ValidationError]()
    // ==================
    // firstCounterparty

    // ==================
    // secondCounterparty

    // ==================
    // terms

    // ==================
    // conditions

    errors.toSeq
  }

  def validated(failFast: Boolean = false): scala.util.Try[CreateContractRequest] = {
    validationErrors(Vector(), failFast) match {
      case Seq()            => Success(asModel)
      case first +: theRest => Failure(ValidationErrors(first, theRest))
    }
  }

  /** use 'validated' to check validation */
  def asModel: CreateContractRequest = {
    CreateContractRequest(
      firstCounterparty = firstCounterparty,
      secondCounterparty = secondCounterparty,
      terms = terms,
      conditions = conditions
    )
  }
}

object CreateContractRequestData {

  given readWriter: RW[CreateContractRequestData] = macroRW

  def fromJsonString(jason: String): CreateContractRequestData = try {
    read[CreateContractRequestData](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason': $e")
  }

  def manyFromJsonString(jason: String): Seq[CreateContractRequestData] = try {
    read[List[CreateContractRequestData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as list: $e")
  }

  def manyFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Seq[CreateContractRequest]] = {
    Try(manyFromJsonString(jason)).flatMap { list =>
      list.zipWithIndex.foldLeft(Try(Vector[CreateContractRequest]())) {
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

  def mapFromJsonString(jason: String): Map[String, CreateContractRequestData] = try {
    read[Map[String, CreateContractRequestData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as map: $e")
  }

  def mapFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Map[String, CreateContractRequest]] = {
    Try(mapFromJsonString(jason)).flatMap { map =>
      map.foldLeft(Try(Map[String, CreateContractRequest]())) {
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
