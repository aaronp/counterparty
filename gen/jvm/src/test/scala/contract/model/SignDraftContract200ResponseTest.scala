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

class SignDraftContract200ResponseTest extends AnyWordSpec with Matchers {

  "SignDraftContract200Response.fromJson" should {
    """not parse invalid json""" in {
      val Failure(err) = Try(SignDraftContract200ResponseData.fromJsonString("invalid jason"))
      err.getMessage should startWith("Error parsing json 'invalid jason'")
    }
    """parse """ ignore {
      val Failure(err: ValidationErrors) =
        SignDraftContract200ResponseData.fromJsonString("""""").validated()

      sys.error("TODO")
    }
  }

}
