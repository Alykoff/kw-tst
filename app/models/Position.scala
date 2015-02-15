package models

import play.api.libs.json.Json

case class Position(id: String, count: Long) extends Ordered[Position] {
  import scala.math.Ordered.orderingToOrdered
  def compare(that: Position): Int = this.id compare that.id
}

object Position {
  implicit val positionFormat = Json.format[Position]

  implicit val ord = new Ordering[Position] {
    override def compare(x: Position, y: Position): Int = x.id compare y.id
  }
}
