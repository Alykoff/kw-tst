package views.util.formdata

import models.Users
import play.Play
import play.api.data.Form
import play.api.data.Forms._

case class LoginData(email: String, password: String)

object LoginData {
  val MIN_PASSWORD_LEN = Play.application().configuration().getInt("form.min_password_len")
  val MAX_PASSWORD_LEN = Play.application().configuration().getInt("form.max_password_len")

  val EMAIL_FIELD_NAME = "signim_email"
  val PASSWORD_FIELD_NAME = "signim_pswrd"

  val loginForm = Form(
    mapping(
      EMAIL_FIELD_NAME -> email,
      PASSWORD_FIELD_NAME -> nonEmptyText(MIN_PASSWORD_LEN, MAX_PASSWORD_LEN)
    )(LoginData.apply)(LoginData.unapply)
    verifying("Invalid user name or password", { fields => fields match {
      case LoginData(eMail, password) =>
        Users.getByEmail(eMail).filter(_.password == password).isDefined
    }})
  )
}