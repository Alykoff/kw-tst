package views.util.formdata

import models.Users
import play.Play
import play.api.data.Form
import play.api.data.Forms._

case class SignUpData(username: String, email: String, password: String)

object SignUpData {
  def checkDublicateUser(username: String, email: String, password: String): Option[SignUpData] = {
    // TODO check user email
    email match {
      case _ => Some(SignUpData(username, email, password))
    }
  }

  val MIN_PASSWORD_LEN = Play.application().configuration().getInt("form.min_password_len")
  val MAX_PASSWORD_LEN = Play.application().configuration().getInt("form.max_password_len")

  val USERNAME_FIELD_NAME = "signup_username"
  val EMAIL_FIELD_NAME = "signup_email"
  val PASSWORD_FIELD_NAME = "signup_pswrd"
  val loginForm = Form(
    mapping(
      EMAIL_FIELD_NAME -> email,
      PASSWORD_FIELD_NAME -> nonEmptyText(MIN_PASSWORD_LEN, MAX_PASSWORD_LEN)
    )(LoginData.apply)(LoginData.unapply)
      verifying("Current user is already signed up!", { fields => fields match {
        case SignUpData(username, eMail, password) => checkDublicateUser(username, eMail, password).isDefined
      }
    })
  )
}