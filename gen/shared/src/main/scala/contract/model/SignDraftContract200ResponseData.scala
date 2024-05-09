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

/** SignDraftContract200ResponseData a data transfer object, primarily for simple json
  * serialisation. It has no validation - there may be nulls, values out of range, etc
  */
case class SignDraftContract200ResponseData(
    /* Indicates if counterparty A has signed the contract. */
    counterpartyASigned: Boolean,

    /* Indicates if counterparty B has signed the contract. */
    counterpartyBSigned: Boolean,

    /* Reason why counterparty A has not signed the contract. */
    counterpartyANotSignedReason: String = "",

    /* Reason why counterparty B has not signed the contract. */
    counterpartyBNotSignedReason: String = ""
) {

  def asJson: String = write(this)

  def validationErrors(path: Seq[Field], failFast: Boolean): Seq[ValidationError] = {
    val errors = scala.collection.mutable.ListBuffer[ValidationError]()
    // ==================
    // counterpartyASigned

    // ==================
    // counterpartyBSigned

    // ==================
    // counterpartyANotSignedReason

    // ==================
    // counterpartyBNotSignedReason

    errors.toSeq
  }

  def validated(failFast: Boolean = false): scala.util.Try[SignDraftContract200Response] = {
    validationErrors(Vector(), failFast) match {
      case Seq()            => Success(asModel)
      case first +: theRest => Failure(ValidationErrors(first, theRest))
    }
  }

  /** use 'validated' to check validation */
  def asModel: SignDraftContract200Response = {
    SignDraftContract200Response(
      counterpartyASigned = counterpartyASigned,
      counterpartyBSigned = counterpartyBSigned,
      counterpartyANotSignedReason = Option(
        counterpartyANotSignedReason
      ),
      counterpartyBNotSignedReason = Option(
        counterpartyBNotSignedReason
      )
    )
  }
}

object SignDraftContract200ResponseData {

  given readWriter: RW[SignDraftContract200ResponseData] = macroRW

  def fromJsonString(jason: String): SignDraftContract200ResponseData = try {
    read[SignDraftContract200ResponseData](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason': $e")
  }

  def manyFromJsonString(jason: String): Seq[SignDraftContract200ResponseData] = try {
    read[List[SignDraftContract200ResponseData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as list: $e")
  }

  def manyFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Seq[SignDraftContract200Response]] = {
    Try(manyFromJsonString(jason)).flatMap { list =>
      list.zipWithIndex.foldLeft(Try(Vector[SignDraftContract200Response]())) {
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

  def mapFromJsonString(jason: String): Map[String, SignDraftContract200ResponseData] = try {
    read[Map[String, SignDraftContract200ResponseData]](jason)
  } catch {
    case NonFatal(e) => sys.error(s"Error parsing json '$jason' as map: $e")
  }

  def mapFromJsonStringValidated(
      jason: String,
      failFast: Boolean = false
  ): Try[Map[String, SignDraftContract200Response]] = {
    Try(mapFromJsonString(jason)).flatMap { map =>
      map.foldLeft(Try(Map[String, SignDraftContract200Response]())) {
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