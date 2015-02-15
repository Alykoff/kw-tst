package models

import controllers.OrderController.EditOrder
import play.api.Logger
import play.api.Play.current

import scala.collection.immutable.TreeSet
import utils.Const._
import org.reactivecouchbase.CouchbaseRWImplicits.documentAsJsObjectReader
import org.reactivecouchbase.CouchbaseRWImplicits.jsObjectToDocumentWriter
import org.reactivecouchbase.Couchbase
import org.reactivecouchbase.CouchbaseBucket
import org.reactivecouchbase.client.OpResult
import org.reactivecouchbase.ReactiveCouchbaseDriver
import org.reactivecouchbase.play._
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}


import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError

import play.api.libs.functional.syntax._
import play.api.libs.functional._

import scala.concurrent.Future
import scala.util.Success
import utils.Utils.createToken

case class Order(id: String, idUser: String, status: Boolean, positions: List[Position]) {

//  def getUpdatedOrder(newOrder: Set[Position]): Order = {
//    newOrder match {
//      case items if items.isEmpty => this
//      case items => getUpdatedOrder(TreeSet[Position]() ++ items)
//    }
//  }

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

  private def getUpdatedOrder(newPositions: Set[Position]): Order = {
???
//    def foldLeft = positions.foldLeft(TreeSet.empty[Position], newPositions) _
//    val (updatedItems, newItems) = foldLeft { case ((acc, updatedPositions), value) =>
//      updatedPositions match {
//        case updatedPositions: TreeSet[Position] if updatedPositions.isEmpty => (acc + value, updatedPositions)
//        case `updatedPositions` => accValue(acc, updatedPositions, value)
//      }
//    }
//
//    val items = (updatedItems ++ newItems).filter(_.count >= 0L)
//    Order(id, idUser, status, items)
  }
}

object Order {
  implicit val positionFormat = Json.format[Position]
//  implicit def set2TreeSet[A](set: Set[A])(implicit ev1: Ordering[A]): TreeSet[A] = {
//    set.foldLeft(TreeSet.empty[A]) {case (acc, x) =>
//      acc + x
//    }
//  }
  implicit def set2TreeSet[A](set: Set[A])(implicit ev1: Ordering[A]): TreeSet[A] = {
    set.foldLeft(TreeSet.empty[A]) { case (acc, x) =>
      acc + x
    }
  }

//  implicit object TweetFormat extends Format[TreeSet[Position]] {
//
//    // convert from Tweet object to JSON (serializing to JSON)
//    def writes(tree: TreeSet[Position]): JsValue = {
//      //  tweetSeq == Seq[(String, play.api.libs.json.JsString)]
//      val tweetSeq = Set(
//        "username" -> JsString(tweet.username),
//        "tweet" -> JsString(tweet.tweet),
//        "date" -> JsString(tweet.date)
//      )
//      JsObject(tweetSeq)
//    }
//
//    // convert from JSON string to a Tweet object (de-serializing from JSON)
//    // (i don't need this method; just here to satisfy the api)
//    def reads(json: JsValue): JsResult[Tweet] = {
//      JsSuccess(Tweet("", "", ""))
//    }
//  }


//  implicit val positionReads: Reads[Position] = (
//    (__ \ "id_item").read[Long] ~
//    (__ \ "count").read[Long](min(0L))
//  )(Position.apply _)
//
//  implicit val positionWrites: Writes[Position] = (
//    (__ \ "id_item").write[Long] ~
//    (__ \ "count").write[Long]
//  )(unlift(Position.unapply))


//  implicit val orderReads: Reads[Order] = (
//    (__ \ "id_order").read[String] ~
//    (__ \ "id_user").read[String] ~
//    (__ \ "status").read[Boolean] ~
//    (__ \ "positions").lazyRead(Reads.list[Position](positionReads))
//  ) (Order)
//
//  implicit val orderWrites: Writes[Order] = (
//    (__ \ "id_order").write[String] ~
//    (__ \ "id_user").write[String] ~
//    (__ \ "status").write[Boolean] ~
//    (__ \ "positions").lazyWrite(list[Position](positionWrites))
//  )(unlift(Order.unapply))

//  implicit val listPositionFormat = Json.format[List[Position]]
  implicit val orderFormat = Json.format[Order]
  implicit val ec = PlayCouchbase.couchbaseExecutor
  def bucket = PlayCouchbase.bucket("default")

  def create(items: List[Position], userId: String) = {
//    val order = Order(nextId, userId, false, items.toList)
//    orders = order :: orders
//    Logger.info(order.toString)
//    order

    val uuid = createToken
    val order = Order(uuid, userId, status = false, items)
    val result: Future[OpResult] = bucket.set[Order](uuid, order)
    result.map {case x =>
      if (x.isSuccess) Some(order)
      else Option.empty[User]
    }
  }

  var orders = Order("1", "1", false, List(Position(1, 4), Position(2, 4), Position(3, 4), Position(6, 4))) ::
    Order("2", "1", false, List(Position(1, 4))) :: Order("3", "2", false, List(Position(1, 4))) :: List()

  def getById(idOrder: String): Future[Option[Order]] = {
    bucket.get[Order](idOrder).recover{case e: Throwable => Option.empty[Order]}
  }

  def getByUserId(idUser: Long): List[Order] = {
    orders.filter(_.idUser == idUser)
  }

  def edit(oldOrder: Order, updatedOrder: EditOrder): Option[Order] = {???
//    val newOrder = oldOrder.getUpdatedOrder(updatedOrder.items)
//    if (orders.filter(_.id == newOrder.id).nonEmpty)
//      orders = (newOrder :: orders.takeWhile(_.id != newOrder.id)) ::: orders.dropWhile(_.id == newOrder.id)
//    Some(newOrder)
  }

  def check(idOrder: String): Boolean = {
    orders.find(_.id == idOrder) match {
      case Some(order) =>
        Store.checkOrder(order.positions)
        orders.foldLeft(List.empty[Order]) {case (acc, x) =>
          if (x.id == idOrder) Order(x.id, x.idUser, true, x.positions) :: acc
          else acc
        }
        true
      case _ => false
    }
  }
}