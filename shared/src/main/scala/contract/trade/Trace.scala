package contract.trade

import zio.*
import scala.reflect.ClassTag

case class CallStack(calls: Seq[Call] = Nil)

/** @param namespace
  *   the namespace of the app - this is needed to group our systems
  * @param app
  *   the name of the system - what or who is invoking the call
  */
final case class Coords(namespace: String, app: String)
object Coords {
  def default(app: String): Coords = Coords("default", app)
  def apply[A: ClassTag](svc: A): Coords = {
    val name = summon[ClassTag[A]].runtimeClass.getSimpleName
    val pck  = summon[ClassTag[A]].runtimeClass.getName
    new Coords(pck, name)
  }
  def apply[A: ClassTag]: Coords = {
    val name = summon[ClassTag[A]].runtimeClass.getSimpleName
    val pck  = summon[ClassTag[A]].runtimeClass.getName
    new Coords(pck, name)
  }
}

final case class CallResponse(timestamp: Long, result: Any)

/** Represents a Call invocation -- something we'd want to capture in our archtiecture (i.e. a
  * sequence diagram describing our system)
  *
  * @param source
  * @param target
  * @param operation
  * @param input
  *   the input which triggered this call
  * @param timestamp
  *   the time the call was made
  */
final case class Call(
    source: Coords,
    target: Coords,
    operations: String,
    input: Any,
    timestamp: Long,
    response: Ref[CallReponse]
)
