package controllers

import models._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Writes._
import scala.util.Success

import models.Order.positionFormat

object StoreController extends Controller {
  val errStatus = "status" -> "err"
  val msgErrSave = Json.obj("status" -> "err", "message" -> "Don't create or save.")
  def msgSuccessSavedUser(name: String) = Json.obj("status" -> "ok", "message" -> ("User '" + name + "' saved."))
  val itemsInPage = 30

  implicit val productFormat = Json.format[ProductType]
  implicit val storeFormat = Json.format[Store]

  def getByPage(page: Long) = Action{ implicit request =>
    val store = Store.get(page * itemsInPage, page * (itemsInPage + 1))
    store match {
      case Success(value) => Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(value)))
      case _ => BadRequest(Json.obj("status" -> "err", "message" -> "bad data."))
    }
  }

  def get = Action { implicit request =>
    Store.getAll match {
      case Some(store) => Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(store)))
      case None => BadRequest(UserController.msgErr("bad data."))
    }
  }
}
