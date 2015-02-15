package views.util.formdata

import models.User
import play.Play
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Await

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
    verifying("Invalid user name or password", {fields => fields match {
      case LoginData(eMail, password) =>
        Logger.debug("in valid meth")
        val user = User.getByEmail(eMail)
        val isAuth = user.map({
          case Some(u) => {
            Logger.debug(u.toString)
            u.password == password
          }
          case None => {
            Logger.debug(s"non validate")
            false
          }
        }) recover {case e: Throwable =>
          Logger.debug("trowable ser!" + e)
          false}
        Await.result(isAuth, 2 second)
    }})
  )
}