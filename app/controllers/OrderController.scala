package controllers

import play.api.mvc.{Action, Controller}
import controllers.SecureController.Authenticated

object OrderController extends Controller {
  def create() = Authenticated { implicit request =>
    Ok(request.user.email)
  }

  def edit(id: Long) = Action {
    Ok("")
  }

  def get(id: Long) = Action {
    Ok("")
  }

  def order = Action {implicit request =>
    Ok(views.html.order())
  }

  def o(id: Long, ud: Long) = Action {
    Ok("")
  }

}
