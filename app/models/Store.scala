package models

case class Store(position: Seq[ProductPosition])

object Store {
  var store = ProductPosition(1, 12) :: ProductPosition(2, 22) :: List()

  def get(from: Long, to: Long) = store
}
