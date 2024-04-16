//> using scala "3.3.1"
//> using lib "counterparty.service::counterparty-service:0.0.1-SNAPSHOT"
//> using repositories https://maven.pkg.github.com/aaronp/counterparty

package contract.server

import contract.DraftContract
import counterparty.service.server.api.*
import counterparty.service.server.model.SignDraftContractRequest
import counterparty.service.server.{BaseApp, model}

/**
 * this is our business logic for our REST service.
 *
 * It's ... tiny. It just delegates to our services
 * @param draftLogic the business logic for draft signatures
 * @param signLogic the business logic for signing
 */
class Service(
               draftLogic: InMemoryEnv = InMemoryEnv(),
               signLogic : SignContractInMemory = SignContractInMemory()) extends DefaultService {
  override def createContract(request: DraftContract) = {
    draftLogic.run(request).execOrThrow()
  }
  override def signDraftContract(request: SignDraftContractRequest) = signLogic.run(request).execOrThrow()
}

/** our main entry point */
object Server extends BaseApp(appDefaultService = Service()):
  start()

