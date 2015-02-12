package models

import controllers.OrderController.{CreateOrder, EditOrder}
import play.api.Logger

import scala.collection.immutable.TreeSet
import scala.util.{Failure, Success, Try}

case class Position(idProduct: Long, count: Long) extends Ordered[Position] {
  import scala.math.Ordered.orderingToOrdered
  def compare(that: Position): Int = this.idProduct compare that.idProduct
}

case class ProductType(idProduct: Long, name: String, cost: Double)
case class Store(positions: TreeSet[Position])

case class Order(idOrder: Long, idUser: Long, status: Boolean, positions: TreeSet[Position]) {
  val BIGGER = 1
  val SMALLER = -1
  val EAQUALS = 0

  def getUpdatedOrder(newOrder: Set[Position]): Order = {
    newOrder match {
      case items if items.isEmpty => this
      case items => getUpdatedOrder(TreeSet[Position]() ++ items)
    }
  }

  private def gt(value: Position)(other: Position) = value.compare(other) == BIGGER

  private def accValue(acc: TreeSet[Position], updatedPositions: TreeSet[Position], value: Position) = {
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

  private def getUpdatedOrder(newPositions: TreeSet[Position]): Order = {

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
  val BIGGER = 1
  val SMALLER = -1
  val EAQUALS = 0

  var store = Store(TreeSet(Position(1, 12)) + Position(2, 22))

  def get(from: Long, to: Long) = Success(store)
  def getAll: Option[Store] = Some(store)

  def checkOrder(items: TreeSet[Position]) = {
    val result = store.positions.foldLeft((TreeSet.empty[Position], items)) {case ((acc, checkedItems), value) =>
      checkedItems match {
        case checkedItems: TreeSet[Position] if checkedItems.isEmpty => (acc + value, checkedItems)
        case `checkedItems` => accValue(acc, checkedItems, value)
      }
    }
    store = Store(result._1)
    Logger.info(s"Store: ${store}")
  }

  private def gt(value: Position)(other: Position) = value.compare(other) == BIGGER

  private def accValue(acc: TreeSet[Position], updatedPositions: TreeSet[Position], value: Position) = {
    val updatedMin = updatedPositions.min
    value.compare(updatedMin) match {
      case BIGGER =>
        val newAcc = acc ++ updatedPositions.takeWhile(gt(value)) + value
        val newUpdatedSet = updatedPositions.dropWhile(gt(value))
        (newAcc, newUpdatedSet)
      case SMALLER => (acc + value, updatedPositions)
      case EAQUALS => {
        val newAcc = acc + Position(value.idProduct, value.count - updatedMin.count)
        val newUpdatedSet = updatedPositions.drop(1)
        (newAcc, newUpdatedSet)
      }
    }
  }
}

object Order {
  implicit def set2TreeSet[A](set: Set[A])(implicit ev1: Ordering[A]): TreeSet[A] = {
    set.foldLeft(TreeSet.empty[A]) { case (acc, x) =>
      acc + x
    }
  }

  def nextId: Long = {
    Option(orders.max(Ordering[Long].on[Order](_.idOrder))).map(_.idOrder + 1).getOrElse(1)
  }
  def create(items: Set[Position], userId: Long) = {
    val order = Order(nextId, userId, false, items)
    orders = order :: orders
    Logger.info(order.toString)
    order
  }

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

  def check(idOrder: Long): Boolean = {
    orders.find(_.idOrder == idOrder) match {
      case Some(order) =>
        Store.checkOrder(order.positions)
        orders.foldLeft(List.empty[Order]) {case (acc, x) =>
          if (x.idOrder == idOrder) Order(x.idOrder, x.idUser, true, x.positions) :: acc
          else acc
        }
        true
      case _ => false
    }
  }
}