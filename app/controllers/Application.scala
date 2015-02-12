package controllers

import models.User
import play.api.Logger
import play.api.mvc._
import views.util.formdata.{SignUpData, LoginData}
import play.api.Play.current
import controllers.SecureController.{createToken, createSecuritySession, delSecuritySession, AUTH_TOKEN, AUTH_TOKEN_HEADER}

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
        val token = createToken
        createSecuritySession(token, loginData)
        Redirect(routes.OrderController.get(1))
          .withSession(AUTH_TOKEN -> token)
          .withHeaders(AUTH_TOKEN_HEADER -> token)
      }
    )
  }

  def signup = Action {implicit request =>
    SignUpData.loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(LoginData.loginForm, formWithErrors))
      },
      userData => {
        User.create(userData.username, userData.email, userData.password)
        Redirect(routes.Application.index)
      }
    )
  }

}
