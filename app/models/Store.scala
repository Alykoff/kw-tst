package models

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

case class Store(positions: List[Position])

object Store { self =>
  var store = Store(List(Position("1", 12), Position("2", 22)))
  implicit val ec = PlayCouchbase.couchbaseExecutor
  def bucket = PlayCouchbase.bucket("default")

  def get(from: Int, to: Int): Future[Store] =  {
    if (from >= to) Future(Store(List.empty[Position]))
    else {
      val query = new Query()
        .setIncludeDocs(true)
        .setStale(Stale.FALSE)
        .setLimit(from - to).setSkip(from)
      val store = bucket.find[Position]("dev_stores", "by_id")(query)
        .map(Store(_))
        .recover { case e: Throwable => {
        Logger.warn(e.toString)
        Store(List.empty[Position])
      }
      }
      store.map { case x => Logger.debug(x.toString)}
      store
    }
  }

  def getAll: Future[Store] = {
    val query = new Query()
      .setIncludeDocs(true)
      .setStale(Stale.FALSE)
    val store = bucket.find[Position]("dev_stores", "by_id")(query)
      .map(Store(_))
      .recover{case e: Throwable => {
        Logger.warn(e.toString)
        Store(List.empty[Position])
      }}
    store.map{case x => Logger.debug(x.toString)}

    store
  }

  def checkOrder(inItems: List[Position]): Future[Boolean] = {
    val inSortedItems = inItems.sortBy(_.id)
    val editedItems = self.getAll.map{ case _store =>
      Try {
        val intersectStoreWithItems = _store.positions.sortBy(_.id).foldLeft((List.empty[Position], inSortedItems)) { case ((acc, newItems), item) =>
          newItems match {
            case newItems: List[Position] if newItems.isEmpty => (acc, newItems)
            case `newItems` => accValue(acc, newItems, item)
          }
        }
        val notFoundItems = intersectStoreWithItems._2.nonEmpty
        if (notFoundItems) throw new NotFoundAllItemsException
        val findEditedItems = intersectStoreWithItems._1
        findEditedItems
      }
    }
    editedItems flatMap {{
      case Success(items) =>
        val rawAllResults: List[Future[OpResult]] =
          for (item: Position <- items) yield bucket.set[Position](item.id, item)
        val allResults: Future[Boolean] = Future.sequence(rawAllResults).map {
          _.foldLeft(true) { case (acc, x) =>
            x.isSuccess && acc
          }
        }.recover { case e: Throwable =>
          false
        }
        allResults
      case Failure(e) =>
        Future(false)
    }}
  }

  private def gt(value: Position)(other: Position) = value.compare(other) == BIGGER

  private def accValue(acc: List[Position], updatedPositions: List[Position], value: Position) = {
    val updatedMin = updatedPositions.min
    value.compare(updatedMin) match {
      case BIGGER | SMALLER => (acc, updatedPositions)
      case EAQUALS =>
        val newCount = value.count - updatedMin.count
        if (newCount < 0) throw new NegativeCountException
        val newAcc = acc :+ Position(value.id, newCount)
        val newUpdatedSet = updatedPositions.drop(1)
        (newAcc, newUpdatedSet)
    }
  }

  class NotFoundAllItemsException extends Exception
  class NegativeCountException extends Exception
}
