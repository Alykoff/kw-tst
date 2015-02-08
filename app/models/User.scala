package models

/**
 *
 * @author alykoff
 *         07.02.2015
 */
case class User(id: Long, name: String, password: String)

object Users {
  def isEmailExist(email: String) = {
    true
  }

  def get(email: String, password: String): Option[User] = {
    email match {
      case _ => Some(User(1, email, password))
    }
  }
}