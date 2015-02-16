package models

import scala.collection.immutable.TreeSet
import org.reactivecouchbase.CouchbaseRWImplicits.documentAsJsObjectReader
import org.reactivecouchbase.CouchbaseRWImplicits.jsObjectToDocumentWriter
import org.reactivecouchbase.Couchbase
import org.reactivecouchbase.CouchbaseBucket
import org.reactivecouchbase.client.OpResult
import org.reactivecouchbase.ReactiveCouchbaseDriver
import org.reactivecouchbase.play._
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}

import utils.Const._
import play.api.libs.json._
import play.api.Logger
import play.api.Play.current

import scala.concurrent.Future
import utils.Utils.createToken

case class Order(id: String, idUser: String, status: Boolean, positions: List[Position]) {

  def getUpdatedOrder(newOrder: List[Position]): Order = {
    newOrder match {
      case items if items.isEmpty => this
      case items => getHelperUpdatedOrder(items)
    }
  }

  private def gt(value: Position)(other: Position) = value.compare(other) == BIGGER

  private def accValue(acc: List[Position], updatedPositions: List[Position], value: Position) = {
    val updatedMin = updatedPositions.min
    value.compare(updatedMin) match {
      case BIGGER =>
        val newAcc = acc ::: (updatedPositions.takeWhile(gt(value)) :+ value)//.sortBy(_.id) //TODO !!!
        val newUpdatedSet = updatedPositions.dropWhile(gt(value))
        (newAcc, newUpdatedSet)
      case SMALLER => (acc :+ value, updatedPositions)
      case EAQUALS =>
        val newAcc = acc :+ Position(value.id, updatedMin.count)
        val newUpdatedSet = updatedPositions.drop(1)
        (newAcc, newUpdatedSet)
    }
  }

  private def getHelperUpdatedOrder(newPositions: List[Position]): Order = {
    val sortedNewPos = newPositions.sortBy(_.id)
    def foldLeft = positions.foldLeft(List.empty[Position], sortedNewPos) _
    val (updatedItems, newItems) = foldLeft { case ((acc, updatedPositions), value) =>
      updatedPositions match {
        case updatedPositions: List[Position] if updatedPositions.isEmpty => (acc :+ value, updatedPositions)
        case `updatedPositions` => accValue(acc, updatedPositions, value)
      }
    }

    val items = (updatedItems ::: newItems).filter(_.count >= 0L)
    Order(id, idUser, status, items)
  }
}

object Order { self =>
  implicit val orderFormat = Json.format[Order]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  def bucket = PlayCouchbase.bucket("default")

  def create(items: List[Position], userId: String) = {
    val uuid = createToken
    val order = Order(uuid, userId, status = false, items)
    val result: Future[OpResult] = bucket.set[Order](uuid, order)
    result.map {case x =>
      if (x.isSuccess) Some(order)
      else Option.empty[User]
    }
  }

  var orders = Order("1", "1", false, List(Position("1", 4), Position("2", 4), Position("3", 4), Position("6", 4))) ::
    Order("2", "1", false, List(Position("1", 4))) :: Order("3", "2", false, List(Position("1", 4))) :: List()

  def getById(idOrder: String): Future[Option[Order]] = {
    bucket.get[Order](idOrder).recover{case e: Throwable => {
      Logger.warn(e.getMessage)
      Option.empty[Order]
    }}
  }

  def getByUserId(idUser: Long): List[Order] = {
    orders.filter(_.idUser == idUser)
  }

  def edit(oldOrder: Order, items: List[Position], status: Boolean): Future[Option[Order]] = {
    val id = oldOrder.id
    val order = Order(id, oldOrder.idUser, oldOrder.status, items)
    bucket.set[Order](id, order).map {case x =>
      if (x.isSuccess) Some(order)
      else Option.empty[Order]
    }.recover{case e: Throwable =>
      Logger.warn(e.getMessage)
      Option.empty[Order]
    }
  }

  def check(idOrder: String): Future[Boolean] = {
    self.getById(idOrder).map{{
      case Some(order) =>
        Store.checkOrder(order.positions) // TODO !!!
        self.edit(order, order.positions, status = true)
        true
      case _ => false
    }}
  }
}