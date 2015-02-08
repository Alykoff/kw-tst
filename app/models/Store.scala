package models

case class Store(idProduct: Long, numberOfProducts: Long)

object Store {
  var stores = Store(1L, 12L) :: Store(2L, 1L) :: List()

  def getByIdProduct(idProduct: Long) = {
    stores.find(_.idProduct == idProduct)
  }
}
