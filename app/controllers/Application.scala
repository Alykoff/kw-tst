package controllers

import models.{Users, User}
import play.Play
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import views.util.formdata.{SignUpData, LoginData}

object Application extends Controller {


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
        Redirect(routes.Application.index)
      }
    )
  }

  def signup = Action {implicit request =>
    SignUpData.loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(LoginData.loginForm, formWithErrors))
      },
      userData => {
        /* binding success, you get the actual value. */
        //        val newUser = models.User(userData.name, userData.age)
        //        val id = models.User.create(newUser)
        //        request.flash("error")
        Redirect(routes.Application.index)
      }
    )
  }
}
