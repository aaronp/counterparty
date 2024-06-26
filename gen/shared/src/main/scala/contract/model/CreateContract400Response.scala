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

case class CreateContract400Response(
    /* A message detailing the error with the provided input. */
    error: Option[String] = None
) {

  def asJson: String = asData.asJson

  def asData: CreateContract400ResponseData = {
    CreateContract400ResponseData(
      error = error.getOrElse("")
    )
  }

}

object CreateContract400Response {

  given RW[CreateContract400Response] =
    CreateContract400ResponseData.readWriter.bimap[CreateContract400Response](_.asData, _.asModel)

  enum Fields(fieldName: String) extends Field(fieldName) {
    case error extends Fields("error")
  }

}
