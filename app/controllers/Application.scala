package controllers

import models.Users
import play.api.mvc._
import views.util.formdata.{SignUpData, LoginData}
import java.util.UUID.randomUUID

object Application extends Controller {
  val SESSION_KEY = "uuid"

  def index = Action {
    Ok(views.html.index(LoginData.loginForm, SignUpData.loginForm))
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
        Redirect(routes.OrderController.order).withSession(SESSION_KEY -> randomUUID().toString)
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
