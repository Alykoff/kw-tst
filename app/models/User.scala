package models

import play.api.Logger

case class User(id: Long, name: String, email: String, password: String)
case class ThinUser(name: String, email: String, password: String)

object Users {
  val firstId = 0L

  var users = User(0L, "al", "al@me.ru", "111") ::
    User(1L, "b", "b@me.ru", "111") ::
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

  def get(email: String, password: String): Option[User] = {
    users.find(user => user.email == email && user.password == password)
  }

  def getByEmail(email: String): Option[User] = {
    users.find(_.email == email)
  }

  def getById(idUser: Long): Option[User] =
    users.find(_.id == idUser)

  def save(user: User) =
    users = user :: users

}