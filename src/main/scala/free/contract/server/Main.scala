//> using scala "3.3.1"
//> using lib "counterparty.service::counterparty-service:0.0.1-SNAPSHOT"
//> using repositories https://maven.pkg.github.com/aaronp/counterparty

package free.contract


import counterparty.service.server.BaseApp
import counterparty.service.server.api.*
import counterparty.service.server.model.*

import java.io.File

// TODO - write your business logic for your services here (the defaults all return 'not implemented'):
val myDefaultService : DefaultService = DefaultService() // <-- replace this with your implementation

/** This is your main entry point for your REST service
 *  It extends BaseApp which defines the business logic for your services
 */
object Server extends BaseApp(appDefaultService = myDefaultService):
  start()

