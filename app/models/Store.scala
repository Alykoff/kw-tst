package models

import controllers.OrderController.{CreateOrder, EditOrder}
import play.api.Logger
import utils.Const._
import play.api.Play.current
import org.reactivecouchbase.CouchbaseRWImplicits.documentAsJsObjectReader
import org.reactivecouchbase.CouchbaseRWImplicits.jsObjectToDocumentWriter
import org.reactivecouchbase.Couchbase
import org.reactivecouchbase.CouchbaseBucket
import org.reactivecouchbase.client.OpResult
import org.reactivecouchbase.ReactiveCouchbaseDriver
import org.reactivecouchbase.play._
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class ProductType(idProduct: Long, name: String, cost: Double)
case class Store(positions: List[Position])

object Store {
  var store = Store(List(Position("1", 12), Position("2", 22)))
  implicit val ec = PlayCouchbase.couchbaseExecutor
  def bucket = PlayCouchbase.bucket("default")

  def get(from: Long, to: Long) = Success(store)

  // TODO !!!
  def getAll: Future[Store] = {
    val query = new Query()
      .setIncludeDocs(true)
      .setStale(Stale.FALSE)
      //.setLimit(1).setOffset()
    val store = bucket.find[Position]("dev_stores", "by_id")(query)
      .map(Store(_))
      .recover{case e: Throwable => {
        Logger.warn(e.toString)
        Store(List.empty[Position])
      }}
    store.map{case x => Logger.debug(x.toString)}

    store
  }

  def checkOrder(items: List[Position]): Future[Boolean] = {
    val result = store.positions.foldLeft((List.empty[Position], items)) {case ((acc, checkedItems), value) =>
      checkedItems match {
        case checkedItems: List[Position] if checkedItems.isEmpty => (acc :+ value, checkedItems)
        case `checkedItems` => accValue(acc, checkedItems, value)
      }
    }
    // TODO !!!
    val rawAllResults: List[Future[OpResult]] =
      for (item: Position <- items) yield bucket.set[Position](item.id, item)
    val allResults: Future[Boolean] = Future.sequence(rawAllResults).map {
         _.foldLeft(true) { case (acc, x) =>
           x.isSuccess && acc
         }
      }.recover { case e: Throwable =>
        Logger.error(e.getMessage)
        false
      }
//    val summaryResult = Future.sequence(allResults).
    allResults
//    store = Store(result._1)
//    Logger.info(s"Store: ${store}")
  }

  private def gt(value: Position)(other: Position) = value.compare(other) == BIGGER

  private def accValue(acc: List[Position], updatedPositions: List[Position], value: Position) = {
    val updatedMin = updatedPositions.min
    value.compare(updatedMin) match {
      case BIGGER =>
        val newAcc = acc ++ updatedPositions.takeWhile(gt(value)) :+ value
        val newUpdatedSet = updatedPositions.dropWhile(gt(value))
        (newAcc, newUpdatedSet)
      case SMALLER => (acc :+ value, updatedPositions)
      case EAQUALS => {
        val newAcc = acc :+ Position(value.id, value.count - updatedMin.count)
        val newUpdatedSet = updatedPositions.drop(1)
        (newAcc, newUpdatedSet)
      }
    }
  }
}
