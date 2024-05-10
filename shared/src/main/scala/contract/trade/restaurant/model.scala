package contract.trade.restaurant

import upickle.default.*

case class DishData(ingredients: List[String]) derives ReadWriter {

  def asIngredients: Seq[Ingredient] = ingredients.map(Ingredient.apply)
  def asDish                         = Dish(asIngredients)
}
case class OrderData(dishes: Seq[DishData]) derives ReadWriter {
  def asOrder: Order = Order(dishes.map(_.asDish))
  def asJson         = write(this)
}

object OrderData {
  val pancake = DishData(List("milk", "butter", "flour", "egg", "egg", "egg"))

  val fishAndChips = DishData(List("fish", "chips"))

  val testOrder = OrderData(List(pancake, fishAndChips))
}
