package free.contract

/**
 * 'EnactContract' is a separate business flow, so has different operations
 *
 * @tparam A this is the generic result type of each operation
 */
enum EnactContractLogic[A]:
  case SignContract(firstCounterpartyRef: CounterpartyRef,
                    secondCounterpartyRef: CounterpartyRef) extends EnactContractLogic[SignContractResponse]
  case RequestSignature(counterpartyRef: CounterpartyRef) extends EnactContractLogic[Option[NotSignedReason]]
  case LogMessage(message: String) extends EnactContractLogic[Unit]

object EnactContractLogic {

}