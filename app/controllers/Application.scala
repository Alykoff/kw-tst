package controllers

import models.Users
import play.api.Logger
import play.api.mvc._
import views.util.formdata.{SignUpData, LoginData}
import play.api.Play.current
import controllers.SecureController.{createToken, createSecuritySession, delSecuritySession, AUTH_TOKEN}

object Application extends Controller {
  def index = Action {
    Ok(views.html.index(LoginData.loginForm, SignUpData.loginForm))
  }

  def logout = Action { implicit request =>
    delSecuritySession
    Redirect(routes.Application.index).withNewSession
  }

  def signin = Action { implicit request =>
    LoginData.loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(formWithErrors, SignUpData.loginForm))
      },
      loginData => {
        /* binding success, you get the actual value. */
        //        val newUser = models.User(userData.name, userData.age)
        //        val id = models.User.create(newUser)
        //        request.flash("error")
        val token = createToken
        createSecuritySession(token, loginData)
        Redirect(routes.OrderController.order).withSession(AUTH_TOKEN -> token)
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
