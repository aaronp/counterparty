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

// this model was generated using model.mustache
package contract.model
import scala.util.control.NonFatal

// see https://com-lihaoyi.github.io/upickle/
import upickle.default.{ReadWriter => RW, macroRW}
import upickle.default.*

case class CreateContractRequest(
    /* The name of the first counterparty in the contract. */
    firstCounterparty: String,

    /* The name of the second counterparty in the contract. */
    secondCounterparty: String,

    /* The terms agreed upon in the contract. */
    terms: String,

    /* The conditions of the contract. */
    conditions: String
) {

  def asJson: String = asData.asJson

  def asData: CreateContractRequestData = {
    CreateContractRequestData(
      firstCounterparty = firstCounterparty,
      secondCounterparty = secondCounterparty,
      terms = terms,
      conditions = conditions
    )
  }

}

object CreateContractRequest {

  given RW[CreateContractRequest] =
    CreateContractRequestData.readWriter.bimap[CreateContractRequest](_.asData, _.asModel)

  enum Fields(fieldName: String) extends Field(fieldName) {
    case firstCounterparty  extends Fields("firstCounterparty")
    case secondCounterparty extends Fields("secondCounterparty")
    case terms              extends Fields("terms")
    case conditions         extends Fields("conditions")
  }

}