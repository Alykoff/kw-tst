package models

import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.concurrent.Future
import play.api.Play.current
import org.reactivecouchbase.Couchbase
import org.reactivecouchbase.CouchbaseBucket
import org.reactivecouchbase.client.OpResult
import org.reactivecouchbase.ReactiveCouchbaseDriver
import org.reactivecouchbase.play._

import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError

import play.api.libs.functional.syntax._

case class User(id: Long, name: String, email: String, password: String) {
//  def save(): Future[OpResult] = User.save(this)
}
case class ThinUser(name: String, email: String, password: String)

object User {
  // get a driver instance driver
  implicit val personFormat = Json.format[User]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  def bucket = PlayCouchbase.bucket("default")

  val firstId = 0L

  var users = User(0L, "al", "al@me.ru", "111") ::
    User(1L, "b", "b@bb.bb", "111") ::
    User(2L, "c", "c@me.ru", "111") ::
    User(3L, "d", "d@me.ru", "111") ::
    User(4L, "e", "e@me.ru", "111") :: List()

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

  def getByEmail(email: String): Option[User] = {
    users.find(_.email == email)
  }

  def getById(idUser: Long): Future[Option[User]] = {
    bucket.get[User]("")
  }

  def save(user: User) = users = user :: users

}