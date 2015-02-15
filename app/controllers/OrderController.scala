package controllers

import models._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Controller}
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

import models.Order.positionFormat
import models.Order.orderFormat

object OrderController extends Controller {

  case class EditOrder(orderId: String, items: List[Position])
  implicit val editOrderFormat = Json.format[EditOrder]

  case class CreateOrder(items: List[Position])
  implicit val createdOrderFormat = Json.format[CreateOrder]

  case class CheckOrder(idOrder: String)
  implicit val checkOrderFormat = Json.format[CheckOrder]

  def msgSuccess(msg: String) = Json.obj("status" -> "ok", "message" -> msg)

  def edit(id: Long) = Authenticated.async(BodyParsers.parse.json) { implicit request =>
    val editOrderResult = request.body.validate[EditOrder]
    editOrderResult.fold(
      error => Future(Ok("Bad data input")),
      editOrder => {
        val user = request.user
        def checkOrder(order: Order) = order.idUser == user.id && !order.status
        val rawOrder = Order.getById(editOrder.orderId).map(_.filter(checkOrder))
        rawOrder.map({
          case None => Ok("order not found")
          case Some(order) =>
            Logger.info(s"editOrder: $editOrder")
            val newOrder = Order.edit(order, editOrder)
            Logger.info(s"editOrder: ${Order.orders}")
            Ok(msgSuccess("saved"))
        })
      }
    )
  }

  def get(id: String) = Authenticated.async{ implicit request =>
    Order.getById(id) map{{
      case Some(order) if order.idUser == request.user.id =>
        Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(order)))
      case Some(order) =>
        BadRequest(UserController.msgErr("Permission error"))
      case None =>
        BadRequest(UserController.msgErr("Not found order"))
    }}
  }

  def create = Authenticated.async(BodyParsers.parse.json) {implicit request =>
    Future {
      request.body.validate[CreateOrder]
    } flatMap {
     _.fold(
        error => Future(BadRequest("Bad data input")),
        createOrder => {
          val userId = request.user.id
          Order.create(createOrder.items, userId).map{{
            case None => BadRequest("Didn't save!")
            case _ => Ok(msgSuccess("saved"))
          }}
        }
      )
    }
  }

  def check(id: String) = Authenticated.async{ implicit request =>
    Order.getById(id).map {{
      case Some(order) if order.idUser == request.user.id =>
        Logger.info(s"order: ${order.positions}")
        Order.check(id)
        Ok(msgSuccess("checked"))
      case Some(order) => BadRequest(UserController.msgErr("Permission error"))
      case _ => BadRequest(UserController.msgErr("Not found order"))
    }}
  }
}
