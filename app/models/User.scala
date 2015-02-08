package models


case class User(id: Long, name: String, email: String, password: String)

object Users {
  def isEmailExist(email: String) = {
    true
  }

  def create(name: String, email: String, password: String): User = {
    User(0L, name, email, password)
  }

  def get(email: String, password: String): Option[User] = {
    email match {
      case _ => Some(User(1, "", email, password))
    }
  }

  def getByEmail(email: String): Option[User] = {
    email match {
      case _ => Some(User(1, "1", email, "1"))
    }
  }

}