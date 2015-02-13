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

case class User(id: String, name: String, email: String, password: String) {
//  def save(): Future[OpResult] = User.save(this)
}
case class ThinUser(name: String, email: String, password: String)

object User {
//  implicit val userReads: Reads[User] = (
//    (__ \ "id").read[String] ~
//    (__ \ "email").read[String](email keepAnd minLength[String](5)) ~
//    (__ \ "name").read[String](minLength[String](2)) ~
//    (__ \ "password").read[String](minLength[String](2))
//  )(User.apply _)
//  implicit val userWrites: Writes[User] = (
//    (__ \ "id").write[String] ~
//    (__ \ "email").write[String] ~
//    (__ \ "name").write[String] ~
//    (__ \ "password").write[String]
//  )(User.apply _)

  implicit val personFormat = Json.format[User]//Format(userReads, userWrites)
//  implicit val userReader = Json.reads[User]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  def bucket = PlayCouchbase.bucket("default")

  val firstId = "0"

  var users = User("0", "al", "al@me.ru", "111") ::
    User("1", "b", "b@bb.bb", "111") ::
    User("2", "c", "c@me.ru", "111") ::
    User("3", "d", "d@me.ru", "111") ::
    User("4", "e", "e@me.ru", "111") :: List()

  def isEmailExist(email: String) = {
    users.filter(_.email == email).nonEmpty
  }
  def create(name: String, email: String, password: String): Option[User] = {
    val nextId = Option(users).map(_.last.id + 1L).getOrElse(firstId)
    val user = User(nextId, name, email, password)
    save(user)
    Some(user)
  }

  def create(thinUser: ThinUser): Option[User] = {
    create(thinUser.name, thinUser.email, thinUser.password)
  }

  def get(email: String, password: String): Option[User] = {
    users.find(user => user.email == email && user.password == password)
  }

  def getByEmail(email: String): Future[Option[User]] = {
//    bucke.find
//    users.find(_.email == email)
    bucket.find[User]("user", "by_email")(new Query().setIncludeDocs(true).setStale(Stale.FALSE).setLimit(1)).map(_.headOption)
  }

  def findAll(email: String) = {
//    val query = new Query()
//      .setLimit(1)
//      .setIncludeDocs(true)
//      .setStale(Stale.FALSE)
//      .setRangeStart(ComplexKey.of(email))
//      .setRangeEnd(ComplexKey.of(s"$email\uefff"))
//    bucket.find[ShortURL]("email", "by_email")(query).map(_.headOption)
//    bucket.find[User]("user", "by_email")(new Query().setIncludeDocs(true).setStale(Stale.FALSE).setLimit(1))
  }

  def getById(idUser: String): Future[Option[User]] = {
    bucket.get[User](idUser)
  }

  def save(user: User) = users = user :: users

}