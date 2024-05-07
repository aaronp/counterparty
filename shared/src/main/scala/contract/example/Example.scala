package contract.example

import contract.*

import scala.util.Try

object Example {

  case class Pizza(toppings: List[String])

  type Money = Double

  enum PizzaOperation[A]:
    case SaveOrder(quantity: Int, toppings: List[String]) extends PizzaOperation[Int]
    case Bake(toppings: List[String])                     extends PizzaOperation[Pizza]
    case Deliver(pizza: Pizza)                            extends PizzaOperation[Money]
    case RecordOrder(orderId: Int, money: Money)          extends PizzaOperation[Unit]

  object PizzaOperation:
    import PizzaOperation.*
    def withFlatMap(quantity: Int, toppings: List[String]): Program[PizzaOperation, Pizza] = {
      Program(SaveOrder(quantity, toppings)).flatMap { orderId =>
        Program(Bake(toppings)).flatMap { pizza =>
          Program(Deliver(pizza)).flatMap { money =>
            Program(RecordOrder(orderId, money)).map { _ => pizza }
          }
        }
      }
    }

    def takeTwo(quantity: Int, toppings: List[String]): Program[PizzaOperation, Pizza] = {
      for
        orderId <- Program(SaveOrder(quantity, toppings))
        pizza   <- Program(Bake(toppings))
        money   <- Program(Deliver(pizza))
        _       <- Program(RecordOrder(orderId, money))
      yield pizza
    }

    def takeThree(quantity: Int, toppings: List[String]): Program[PizzaOperation, Pizza] = {
      for
        orderId <- SaveOrder(quantity, toppings).asProgram
        pizza   <- Bake(toppings).asProgram
        money   <- Deliver(pizza).asProgram
        _       <- RecordOrder(orderId, money).asProgram
      yield pizza
    }

}
