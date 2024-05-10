//> using scala "3.4.1"
//> using lib "com.github.aaronp::counterparty:0.0.1-SNAPSHOT"
//> using repositories https://maven.pkg.github.com/aaronp/counterparty

package contract.server

import contract.model.*
import contract.api.*
import contract.*
import contract.DraftContract
import contract.handler.SignContractHandler
import contract.handler.CreateDraftHandler
import support.given

/** this is our business logic for our REST service.
  *
  * It's ... tiny. It just delegates to our services
  * @param draftLogic
  *   the business logic for draft signatures
  * @param signLogic
  *   the business logic for signing
  */
class Service(
    draftLogic: CreateDraftHandler = contract.handler.CreateDraftHandler.InMemory(),
    signLogic: SignContractHandler = contract.handler.SignContractHandler.InMemory()
) extends DefaultService {
  override def createContract(request: DraftContract) = {
    // draftLogic.run(request).execOrThrow()
    ???
  }
  override def signDraftContract(request: SignDraftContractRequest) =
    signLogic.run(request).execOrThrow()
}

/** our main entry point */
object Server extends BaseApp(appDefaultService = Service()):
  start()
