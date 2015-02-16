package controllers

import models._
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.libs.concurrent.Execution.Implicits._
import utils.Utils.{msgErr, msgOk}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object StoreController extends Controller {
//  val errStatus = "status" -> "err"
//  val msgErrSave = Json.obj("status" -> "err", "message" -> "Don't create or save.")
//  def msgSuccessSavedUser(name: String) = Json.obj("status" -> "ok", "message" -> ("User '" + name + "' saved."))
  val itemsInPage: Int = 2

  implicit val storeFormat = Json.format[Store]

  def getByPage(page: Int) = Action.async{ implicit request =>
    Future {
        val from = page * itemsInPage
        val to = (page + 1) * itemsInPage
        Logger.debug((from, to).toString)
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
