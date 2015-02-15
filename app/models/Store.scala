package models

import controllers.OrderController.{CreateOrder, EditOrder}
import play.api.Logger
import utils.Const._

import scala.util.{Failure, Success, Try}

case class Position(id: String, count: Long) extends Ordered[Position] {
  import scala.math.Ordered.orderingToOrdered
  def compare(that: Position): Int = this.id compare that.id
}

object Position {
  implicit val ord = new Ordering[Position] {
    override def compare(x: Position, y: Position): Int = x.id compare y.id
  }
}

case class ProductType(idProduct: Long, name: String, cost: Double)
case class Store(positions: List[Position])

object Store {
  var store = Store(List(Position("1", 12), Position("2", 22)))

  def get(from: Long, to: Long) = Success(store)
  def getAll: Option[Store] = Some(store)
  // TODO !!!
  def checkOrder(items: List[Position]) = {
    val result = store.positions.foldLeft((List.empty[Position], items)) {case ((acc, checkedItems), value) =>
      checkedItems match {
        case checkedItems: List[Position] if checkedItems.isEmpty => (acc :+ value, checkedItems)
        case `checkedItems` => accValue(acc, checkedItems, value)
      }
    }
    store = Store(result._1)
    Logger.info(s"Store: ${store}")
  }

  private def gt(value: Position)(other: Position) = value.compare(other) == BIGGER

  private def accValue(acc: List[Position], updatedPositions: List[Position], value: Position) = {
    val updatedMin = updatedPositions.min
    value.compare(updatedMin) match {
      case BIGGER =>
        val newAcc = acc ++ updatedPositions.takeWhile(gt(value)) :+ value
        val newUpdatedSet = updatedPositions.dropWhile(gt(value))
        (newAcc, newUpdatedSet)
      case SMALLER => (acc :+ value, updatedPositions)
      case EAQUALS => {
        val newAcc = acc :+ Position(value.id, value.count - updatedMin.count)
        val newUpdatedSet = updatedPositions.drop(1)
        (newAcc, newUpdatedSet)
      }
    }
  }
}
