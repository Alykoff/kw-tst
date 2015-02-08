package models

case class User(id: Long, name: String, email: String, password: String)
case class ThinUser(name: String, email: String, password: String)

object Users {
  val firstId = 0L

  val users = scala.collection.mutable.Set[User] (
    User(0L, "al", "al@me.ru", "111"),
    User(1L, "b", "b@me.ru", "111"),
    User(2L, "c", "c@me.ru", "111"),
    User(3L, "d", "d@me.ru", "111"),
    User(4L, "e", "e@me.ru", "111")
  )

  def isEmailExist(email: String) = {
    users.filter(_.email == email).nonEmpty
  }
  def create(name: String, email: String, password: String): Option[User] = {
    val nextId = Option(users).map(_.last.id + 1L).getOrElse(firstId)
    val user = User(nextId, name, email, password)
    users.add(user)
    Some(user)
  }

  def get(email: String, password: String): Option[User] = {
    val filteredUsers = users.filter(user => user.email == email && user.password == password)
    if (filteredUsers.nonEmpty) Some(filteredUsers.head)
    else None
  }

  def getByEmail(email: String): Option[User] = {
    val filteredUsers = users.filter(user => user.email == email)
    if (filteredUsers.nonEmpty) Some(filteredUsers.head)
    else None
  }

}