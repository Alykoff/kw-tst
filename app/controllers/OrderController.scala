package controllers

import play.api.mvc.{Action, Controller}

/**
 *
 * @author alykoff
 *         07.02.2015
 */
object OrderController extends Controller {
  def create() = Action {
    Ok("")
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
