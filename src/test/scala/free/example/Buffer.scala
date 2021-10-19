package free.example

import free.{Semigroup, State}

case class Buffer(inputs: List[(Operation[_], Any)]):
  override def toString = inputs.reverse.map {
    case (op, result) =>
      s"\t$op returned $result"
  }.mkString("\n\t","\n\t","\n")

object Buffer:
  def apply(op: Operation[_], result: Any): Buffer = new Buffer(List(op -> result))

type BufferState[A] = State[Buffer, A]

given Semigroup[Buffer] with
  def combine(a : Buffer, b : Buffer) = Buffer(a.inputs ++ b.inputs)
