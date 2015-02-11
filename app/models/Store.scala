package models

import scala.collection.immutable.TreeSet
import scala.util.{Failure, Success, Try}

case class Position(idProduct: Long, var count: Long) extends Ordered[Position] {
  import scala.math.Ordered.orderingToOrdered
  def compare(that: Position): Int = this.idProduct compare that.idProduct
}

case class ProductType(idProduct: Long, name: String, cost: Double)
case class Store(positions: TreeSet[Position])
case class Order(idOrder: Long, idUser: Long, status: Boolean, positions: List[Position]) {
//  def getUpdatedOrder(newOrder: Set[Position]) = {
//    newOrder match {
//      case items if items.isEmpty => this
//      case items => getUpdatedOrder(TreeSet[Position]() ++ items)
//    }
//  }
//
//  private def getUpdatedOrder(newPositions: List[Position]): Order = {
//    val BIGGER = 1
//    val SMALLER = -1
//    val EAQUALS = 0
//    def gt(value: Position)(other: Position) = value.compare(other) == 1
//    def accValue(acc: List[Position], updatedPositions: List[Position], value: Position) = {
//      val updatedMin = updatedPositions.min
//      value.compare(updatedMin) match {
//        case BIGGER =>
//          val newAcc = acc :: updatedPositions.takeWhile(gt(value)) ++ value
//          val newUpdatedSet = updatedPositions.dropWhile(gt(value))
//          (newAcc, newUpdatedSet)
//        case SMALLER => (acc + value, updatedPositions)
//        case EAQUALS =>
//          value.count = updatedMin.count
//          (acc + value, updatedPositions.drop(1))
//      }
//    }
//    def foldLeft = positions.foldLeft(Array.empty[Position], newPositions: List[Position])
//    val (updatedItems, newItems) = foldLeft { case ((acc, updatedPositions), value) =>
//      updatedPositions match {
//        case updatedPositions: List[Position] if updatedPositions.isEmpty => (acc + value, updatedPositions)
//        case `updatedPositions` => accValue(acc, updatedPositions, value)
//      }
//    }
//
//    Order(idOrder, idUser, status, updatedItems ++ newItems)
//  }
}

object Store {
  var store = Store(TreeSet(Position(1, 12)) + Position(2, 22))
  def get(from: Long, to: Long) = Success(store)
  def getAll: Option[Store] = Some(store)
}

object Order {
  val orders = Order(1, 1, false, TreeSet(Position(1, 4), Position(2, 4), Position(3, 4), Position(6, 4))) ::
    Order(2, 1, false, TreeSet(Position(1, 4))) :: Order(3, 2, false, TreeSet(Position(1, 4))) :: List()

  def getById(idOrder: Long) = {
    orders.find(_.idOrder == idOrder)
  }

  def getByUserId(idUser: Long): List[Order] = {
    orders.filter(_.idUser == idUser)
  }

}