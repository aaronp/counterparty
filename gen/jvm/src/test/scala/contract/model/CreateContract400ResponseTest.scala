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

// this model was generated using modelTest.mustache
package contract.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.util.*

class CreateContract400ResponseTest extends AnyWordSpec with Matchers {

  "CreateContract400Response.fromJson" should {
    """not parse invalid json""" in {
      val Failure(err) = Try(CreateContract400ResponseData.fromJsonString("invalid jason"))
      err.getMessage should startWith("Error parsing json 'invalid jason'")
    }
    """parse """ ignore {
      val Failure(err: ValidationErrors) =
        CreateContract400ResponseData.fromJsonString("""""").validated()

      sys.error("TODO")
    }
  }

}
