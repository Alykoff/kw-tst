package models

case class Order(idOrder: Long, idUser: Long, items: Seq[StoreItem])

object Order {
  val orders = Order(1, 1, List(StoreItem(1, 4))) :: Order(2, 2, List(StoreItem(1, 4))) :: List()

  def getById(idOrder: Long) = {
    orders.find(_.idOrder == idOrder)
  }

  def getByUserId(idUser: Long): List[Order] = {
    orders.filter(_.idUser == idUser)
  }

}