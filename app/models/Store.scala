package models

case class Position(idProduct: Long, numberOfProducts: Long)
case class ProductType(idProduct: Long, name: String, cost: Double)
case class Store(position: Seq[Position])
case class Order(idOrder: Long, idUser: Long, items: Seq[Position])

object Store {
  var store = Position(1, 12) :: Position(2, 22) :: List()
  def get(from: Long, to: Long) = store
}

object Order {
  val orders = Order(1, 1, List(Position(1, 4))) :: Order(2, 2, List(Position(1, 4))) :: List()

  def getById(idOrder: Long) = {
    orders.find(_.idOrder == idOrder)
  }

  def getByUserId(idUser: Long): List[Order] = {
    orders.filter(_.idUser == idUser)
  }
}