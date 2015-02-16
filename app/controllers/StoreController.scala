package controllers

import models._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.libs.concurrent.Execution.Implicits._

object StoreController extends Controller {
  val errStatus = "status" -> "err"
  val msgErrSave = Json.obj("status" -> "err", "message" -> "Don't create or save.")
  def msgSuccessSavedUser(name: String) = Json.obj("status" -> "ok", "message" -> ("User '" + name + "' saved."))
  val itemsInPage: Int = 2

//  implicit val productFormat = Json.format[ProductType]
  implicit val storeFormat = Json.format[Store]

  def getByPage(page: Int) = Action.async{ implicit request =>
    val store = Store.get(page * itemsInPage, page * (itemsInPage + 1))
    store.map{ value =>
      Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(value)))
    }
  }

  def get = Action.async { implicit request =>
    Store.getAll.map{case store =>
      if (store.positions.isEmpty) BadRequest(Json.obj("status" -> "err", "message" -> "empty result"))
      else Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(store)))
    }
  }
}
