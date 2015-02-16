package controllers

import models._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import utils.Utils.{msgErr, msgOk}

import scala.concurrent.Future

object StoreController extends Controller {
  val ITEMS_IN_PAGE: Int = 2
  implicit val storeFormat = Json.format[Store]

  def getByPage(page: Int) = Action.async{ implicit request =>
    Future {
        val from = page * ITEMS_IN_PAGE
        val to = (page + 1) * ITEMS_IN_PAGE
        Store.get(from, to)
    } flatMap { _.map{
      store => Ok(msgOk(Json.toJson(store)))
    }}
  }

  def get = Action.async { implicit request =>
    Store.getAll.map{case store =>
      if (store.positions.isEmpty) BadRequest(msgErr("empty result"))
      else Ok(msgOk(Json.toJson(store)))
    }
  }
}
