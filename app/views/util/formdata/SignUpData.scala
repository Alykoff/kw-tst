package views.util.formdata

import models.User
import play.api.data.Form
import play.api.data.Forms._

case class SignUpData(username: String, email: String, password: String)

object SignUpData {
  val MIN_PASSWORD_LEN = LoginData.MIN_PASSWORD_LEN
  val MAX_PASSWORD_LEN = LoginData.MAX_PASSWORD_LEN

  val USERNAME_FIELD_NAME = "signup_username"
  val EMAIL_FIELD_NAME = "signup_email"
  val PASSWORD_FIELD_NAME = "signup_pswrd"

  val loginForm = Form(
    mapping(
      USERNAME_FIELD_NAME -> nonEmptyText,
      EMAIL_FIELD_NAME -> email,
      PASSWORD_FIELD_NAME -> nonEmptyText(MIN_PASSWORD_LEN, MAX_PASSWORD_LEN)
    )(SignUpData.apply)(SignUpData.unapply)
    verifying("Current user is already signed up!", { fields => fields match {
      case SignUpData(_, eMail, _) =>
        val user = User.getByEmail(eMail)
        !user.isDefined
    }})
  )
}