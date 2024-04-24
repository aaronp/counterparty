package free.examples.etl.ui

import contract.*

/** A collection of inputs paired with their results
  * @param inputs
  */
case class Buffer(inputs: List[(Any, Any)]):

  def steps = inputs.reverse

  override def toString = steps
    .map { case (input, result) =>
      s"\t$input returned $result"
    }
    .mkString("\n\t", "\n\t", "\n")

object Buffer:
  def apply(op: Any, result: Any): Buffer = new Buffer(List(op -> result))

type BufferState[A] = contract.State[Buffer, A]

given Semigroup[Buffer] with
  def combine(a: Buffer, b: Buffer) = Buffer(a.inputs ++ b.inputs)
