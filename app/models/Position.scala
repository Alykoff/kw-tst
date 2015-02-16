package models

import play.api.Logger
import play.api.libs.json.Json
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

case class Position(id: String, count: Long) extends Ordered[Position] {
  import scala.math.Ordered.orderingToOrdered
  def compare(that: Position): Int = {
    val res = this.id compare that.id
    if (res > 0) BIGGER
    else if (res == 0) EAQUALS
    else SMALLER
  }
}

object Position {
  implicit val positionFormat = Json.format[Position]

  implicit val ec = PlayCouchbase.couchbaseExecutor
  def bucket = PlayCouchbase.bucket("default")

  implicit val ord = new Ordering[Position] {
    override def compare(x: Position, y: Position): Int = x compare y
  }

  def save(position: Position): Future[Boolean] = {
    bucket.set[Position](position.id, position)
      .map(_.isSuccess)
      .recover{case e: Throwable =>
        Logger.warn(e.getMessage)
        false
      }
  }
}
