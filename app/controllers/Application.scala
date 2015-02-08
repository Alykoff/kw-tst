package controllers

import models.{Users, User}
import play.Play
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import views.util.formdata.LoginData

object Application extends Controller {


  def index = Action {
    Ok(views.html.index(LoginData.loginForm))
  }

  def signin = Action { implicit request =>
    LoginData.loginForm.bindFromRequest.fold(
      formWithErrors => {
        //        request.flash("success")
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.index(formWithErrors))
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
