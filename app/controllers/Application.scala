package controllers

import models.Users
import play.api.mvc._
import views.util.formdata.{SignUpData, LoginData}
import java.util.UUID._
import play.api.cache.Cache
import play.api.Play.current
import scala.util.Try

object Application extends Controller {
  val SESSION_KEY = "uuid"

  def index = Action {
    Ok(views.html.index(LoginData.loginForm, SignUpData.loginForm))
  }

  def logout = Action { implicit request =>
    val uuid = request.session.get(SESSION_KEY)
    Try(Cache.remove(uuid.get))
    Redirect(routes.Application.index).withNewSession
  }

  def signin = Action { implicit request =>
    LoginData.loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(formWithErrors, SignUpData.loginForm))
      },
      userData => {
        /* binding success, you get the actual value. */
        //        val newUser = models.User(userData.name, userData.age)
        //        val id = models.User.create(newUser)
        //        request.flash("error")
        val uuid = randomUUID().toString
        val userId = Users.getByEmail(userData.email).map(_.id)
        if (userId.isDefined) Cache.set(uuid, userId)
        Redirect(routes.OrderController.order).withSession(SESSION_KEY -> uuid)
      }
    )
  }

  def signup = Action {implicit request =>
    SignUpData.loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(LoginData.loginForm, formWithErrors))
      },
      userData => {
        Users.create(userData.username, userData.email, userData.password)
        /* binding success, you get the actual value. */
        //        val newUser = models.User(userData.name, userData.age)
        //        val id = models.User.create(newUser)
        //        request.flash("error")
        Redirect(routes.Application.index)
      }
    )
  }
}
