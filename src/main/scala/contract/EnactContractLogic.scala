package contract

import counterparty.service.server.model.SignDraftContract200Response

/**
 * This trait is used to provide a generic 'Async' type for our enact contract logic.
 *
 * It allows us to keep a generic type representation for asynchronous actions, while also ensuring that type is
 * consistent across all of the EnactContractLogic operations.
 *
 * Interpreters should extend this trait and provide a concrete type for the `Async[Result]` type
 */
trait EnactContract {

  type Async[Result]

  /**
   * 'EnactContract' is a separate business flow, so has different operations
   *
   * @tparam A this is the generic result type of each operation
   */
  enum EnactContractLogic[A]:
    case RequestSignatureA(counterpartyRef: CounterpartyRef) extends EnactContractLogic[Async[SignatureResult]]
    case RequestSignatureB(counterpartyRef: CounterpartyRef) extends EnactContractLogic[Async[SignatureResult]]
    case Wait[T](asyncProcess: Async[T]) extends EnactContractLogic[T]
    case LogMessage(message: String) extends EnactContractLogic[Unit]


  object EnactContractLogic:

    import EnactContractLogic.*

    def asResult(signatureA: SignatureResult, signatureB: SignatureResult) = {
      (signatureA, signatureB) match {
        case (refA: CounterpartyRef, refB: CounterpartyRef) => SignDraftContract200Response(true, true)
        case (NotSignedReason(reasonA), refB: CounterpartyRef) => SignDraftContract200Response(false, true, Option(reasonA), None)
        case (refA: CounterpartyRef, NotSignedReason(reasonB)) => SignDraftContract200Response(true, false, None, Option(reasonB))
        case (NotSignedReason(reasonA), NotSignedReason(reasonB)) => SignDraftContract200Response(false, false, Option(reasonA), Option(reasonB))
      }
    }
    
    def apply[T](request: SignContract) = for {
      _ <- LogMessage("Signing contracts").free
      processA <- RequestSignatureA(CounterpartyRef(request.referenceA)).free
      processB <- RequestSignatureB(CounterpartyRef(request.referenceB)).free
      signatureA: SignatureResult <- Wait(processA).free
      signatureB <- Wait(processB).free
    } yield asResult(signatureA, signatureB)

}