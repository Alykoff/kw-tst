package models

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

case class Order(id: String, idUser: String, status: Boolean, positions: List[Position])

object Order { self =>
  implicit val orderFormat = Json.format[Order]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  def bucket = PlayCouchbase.bucket("default")

  def create(items: List[Position], userId: String): Future[Option[Order]] = {
    val uuid = createToken
    val order = Order(uuid, userId, status = false, items)
    val result: Future[OpResult] = bucket.set[Order](uuid, order)
    result.map {case x =>
      if (x.isSuccess) Some(order)
      else Option.empty[Order]
    }
  }

  def getById(idOrder: String): Future[Option[Order]] = {
    bucket.get[Order](idOrder).recover{case e: Throwable => {
      Logger.warn(e.getMessage)
      Option.empty[Order]
    }}
  }

  def getByUserId(idUser: String): Future[List[Order]] = {
    val query = new Query()
      .setIncludeDocs(true)
      .setStale(Stale.FALSE)
      .setRangeStart(ComplexKey.of(idUser))
      .setRangeEnd(ComplexKey.of(s"$idUser\uefff"))
    bucket.find[Order]("dev_orders", "by_user")(query)
      .recover{case e: Throwable => {
        Logger.warn(e.toString)
        List.empty[Order]
      }}
  }

  // TODO transaction. Use "N1QL Basics" betta insert
  def edit(oldOrder: Order, items: List[Position], status: Boolean): Future[Option[Order]] = {
    def saveInDB = {
      val id = oldOrder.id
      val order = Order(id, oldOrder.idUser, status, items)
      val result: Future[Option[Order]] = bucket.set[Order](id, order).map {case x =>
        if (x.isSuccess) Some(order)
        else Option.empty[Order]
      }.recover{
        case e: Throwable =>
          Logger.warn(e.getMessage)
          Option.empty[Order]
      }
      result
    }

    Store.getAll.flatMap{ store =>
      val intersectItems = store.positions.map(_.id).toSet & items.map(_.id).toSet
      if (intersectItems.size != items.size) Future(Option.empty[Order])
      else saveInDB
    }
  }

  // TODO transaction. Use "N1QL Basics" betta insert
  def check(idOrder: String): Future[Boolean] = {
    self.getById(idOrder).map{{
      case Some(order) if !order.status =>
        Store.checkOrder(order.positions)
        self.edit(order, order.positions, status = true)
        true
      case _ => false
    }}
  }
}