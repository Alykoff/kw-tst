package models

case class User(id: Long, name: String, email: String, password: String)

object Users {
  val firstId = 0L

  val users = scala.collection.mutable.Set[User] (
    User(0L, "al", "mail@m.ru", "111"),
    User(1L, "b", "mail@m.ru", "111"),
    User(2L, "c", "mail@m.ru", "111"),
    User(3L, "d", "mail@m.ru", "111"),
    User(4L, "e", "mail@m.ru", "111")
  )

  def isEmailExist(email: String) = {
    users.filter(_.email == email).nonEmpty
  }
  def create(name: String, email: String, password: String): User = {
    val nextId = Option(users).map(_.last.id + 1L).getOrElse(firstId)
    User(nextId, name, email, password)
  }

  def get(email: String, password: String): Option[User] = {
    val filteredUsers = users.filter(user => user.email == email && user.password == password)
    filteredUsers match {
      case filteredUsers.nonEmpty => Some(filteredUsers.head)
      case _ => None
    }
  }

  def getByEmail(email: String): Option[User] = {
    val filteredUsers = users.filter(user => user.email == email)
    filteredUsers match {
      case filteredUsers.nonEmpty => Some(filteredUsers.head)
      case _ => None
    }
  }

}