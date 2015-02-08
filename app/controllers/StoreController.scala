package controllers

import play.api.mvc.{Action, Controller}

/**
 *
 * @author alykoff
 *         07.02.2015
 */
object StoreController extends Controller {
  def get(page: Long) = Action {
    Ok("")
  }
}
