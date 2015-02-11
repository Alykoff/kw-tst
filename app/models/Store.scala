package models

import controllers.OrderController.EditOrder

import scala.collection.immutable.TreeSet
import scala.util.{Failure, Success, Try}

case class Position(idProduct: Long, count: Long) extends Ordered[Position] {
  import scala.math.Ordered.orderingToOrdered
  def compare(that: Position): Int = this.idProduct compare that.idProduct
}

case class ProductType(idProduct: Long, name: String, cost: Double)
case class Store(positions: TreeSet[Position])

case class Order(idOrder: Long, idUser: Long, status: Boolean, positions: TreeSet[Position]) {
  def getUpdatedOrder(newOrder: Set[Position]): Order = {
    newOrder match {
      case items if items.isEmpty => this
      case items => getUpdatedOrder(TreeSet[Position]() ++ items)
    }
  }

  private def getUpdatedOrder(newPositions: TreeSet[Position]): Order = {
    val BIGGER = 1
    val SMALLER = -1
    val EAQUALS = 0
    def gt(value: Position)(other: Position) = value.compare(other) == 1
    def accValue(acc: TreeSet[Position], updatedPositions: TreeSet[Position], value: Position) = {
      val updatedMin = updatedPositions.min
      value.compare(updatedMin) match {
        case BIGGER =>
          val newAcc = acc ++ updatedPositions.takeWhile(gt(value)) + value
          val newUpdatedSet = updatedPositions.dropWhile(gt(value))
          (newAcc, newUpdatedSet)
        case SMALLER => (acc + value, updatedPositions)
        case EAQUALS => {
          val newAcc = acc + Position(value.idProduct, updatedMin.count)
          val newUpdatedSet = updatedPositions.drop(1)
          (newAcc, newUpdatedSet)
        }
      }
    }
    def foldLeft = positions.foldLeft(TreeSet.empty[Position], newPositions) _
    val (updatedItems, newItems) = foldLeft { case ((acc, updatedPositions), value) =>
      updatedPositions match {
        case updatedPositions: TreeSet[Position] if updatedPositions.isEmpty => (acc + value, updatedPositions)
        case `updatedPositions` => accValue(acc, updatedPositions, value)
      }
    }

    val items = (updatedItems ++ newItems).filter(_.count >= 0L)
    Order(idOrder, idUser, status, items)
  }
}

object Store {
  var store = Store(TreeSet(Position(1, 12)) + Position(2, 22))
  def get(from: Long, to: Long) = Success(store)
  def getAll: Option[Store] = Some(store)
}

object Order {
  var orders = Order(1, 1, false, TreeSet(Position(1, 4), Position(2, 4), Position(3, 4), Position(6, 4))) ::
    Order(2, 1, false, TreeSet(Position(1, 4))) :: Order(3, 2, false, TreeSet(Position(1, 4))) :: List()

  def getById(idOrder: Long) = {
    orders.find(_.idOrder == idOrder)
  }

  def getByUserId(idUser: Long): List[Order] = {
    orders.filter(_.idUser == idUser)
  }

  def edit(oldOrder: Order, updatedOrder: EditOrder): Option[Order] = {
    val newOrder = oldOrder.getUpdatedOrder(updatedOrder.items)
    if (orders.filter(_.idOrder == newOrder.idOrder).nonEmpty)
      orders = (newOrder :: orders.takeWhile(_.idOrder != newOrder.idOrder)) ::: orders.dropWhile(_.idOrder == newOrder.idOrder)
    Some(newOrder)
  }
}