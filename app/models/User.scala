package models

import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.concurrent.Future
import play.api.Play.current
import org.reactivecouchbase.CouchbaseRWImplicits.documentAsJsObjectReader
import org.reactivecouchbase.CouchbaseRWImplicits.jsObjectToDocumentWriter
import org.reactivecouchbase.Couchbase
import org.reactivecouchbase.CouchbaseBucket
import org.reactivecouchbase.client.OpResult
import org.reactivecouchbase.ReactiveCouchbaseDriver
import org.reactivecouchbase.play._
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}

//import play.api.libs.json.Format._
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError

import play.api.libs.functional.syntax._
import utils.Utils._

case class User(id: String, name: String, email: String, password: String) {
//  def save(): Future[OpResult] = User.save(this)
}
case class ThinUser(name: String, email: String, password: String)

object User {
  implicit val userFormat = Json.format[User]
  implicit val ec = PlayCouchbase.couchbaseExecutor
  def bucket = PlayCouchbase.bucket("default")

  def createUserId = createToken

  def create(thinUser: ThinUser): Future[Option[User]] = {
    create(thinUser.name, thinUser.email, thinUser.password)
  }

  def create(name: String, email: String, password: String): Future[Option[User]] = {
    val uuid = createUserId
    val user = User(uuid, name, email, password)
    bucket.set[User](uuid, user).map {case x => //TODO
      if (x.isSuccess) Some(user)
      else Option.empty[User]
    }
  }

  def get(email: String, password: String): Future[Option[User]] = {
    getByEmail(email).map(_.filter(_.password == password))
  }

  def getByEmail(email: String): Future[Option[User]] = {
    val query = new Query()
        .setIncludeDocs(true)
        .setStale(Stale.FALSE)
        .setRangeStart(ComplexKey.of(email))
        .setRangeEnd(ComplexKey.of(s"$email\uefff"))
        .setLimit(1)
    bucket.find[User]("dev_users", "by_email")(query)
      .map(_.headOption)
      .recover{case e: Throwable => {
        Logger.warn(e.toString)
        Option.empty[User]
      }}
  }

  def getById(idUser: String): Future[Option[User]] = {
    bucket.get[User](idUser).recover{case e: Throwable =>
      Logger.warn(e.getMessage)
      Option.empty[User]
    }
  }

  def save(user: User): Future[Boolean] = {
    bucket.set[User](user.id, user)
      .map(_.isSuccess)
      .recover{case e: Throwable =>
        Logger.warn(e.getMessage)
        false
      }
  }

}