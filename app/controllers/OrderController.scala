package controllers

import models._
import play.api.mvc.{BodyParsers, Controller}
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import utils.Utils.{msgErr, msgOk}

import scala.concurrent.Future

import models.Order.orderFormat

object OrderController extends Controller {
  case class RequestOrder(items: List[Position])
  implicit val createdOrderFormat = Json.format[RequestOrder]

  case class CheckOrder(idOrder: String)
  implicit val checkOrderFormat = Json.format[CheckOrder]

  def edit(id: String) = Authenticated.async(BodyParsers.parse.json) { implicit request =>
    def handleValidInput(editOrder: RequestOrder) = {
      val user = request.user
      def checkOrder(order: Order) = order.idUser == user.id && !order.status
      val rawOrder = Order.getById(id).map(_.filter(checkOrder))
      rawOrder.flatMap({
        case None => Future(BadRequest("order not found"))
        case Some(order) =>
          Order.edit(order, editOrder.items, status = false).map {{
            case None => BadRequest("Didn't save!")
            case _ => Ok(msgOk("saved"))
          }}
      })
    }

    Future {
      request.body.validate[RequestOrder]
    } flatMap {
      _.fold(
        error => Future(BadRequest("Bad data input")),
        editOrder => handleValidInput(editOrder)
      )
    }
  }

  def get(id: String) = Authenticated.async{ implicit request =>
    Order.getById(id) map{{
      case Some(order) if order.idUser == request.user.id => Ok(msgOk(Json.toJson(order)))
      case Some(order) => BadRequest(msgErr("Permission error"))
      case None => BadRequest(msgErr("Not found order"))
    }}
  }

  def create = Authenticated.async(BodyParsers.parse.json) {implicit request =>
    def handleValidRequest(createOrder: RequestOrder) = {
      val userId = request.user.id
      Order.create(createOrder.items, userId).map {{
        case None => BadRequest(msgErr("Didn't save!"))
        case _ => Ok(msgOk("saved"))
      }}
    }
    Future {
      request.body.validate[RequestOrder]
    } flatMap {
     _.fold(
        error => Future(BadRequest("Bad data input")),
        createOrder => handleValidRequest(createOrder)
      )
    }
  }

  def check(id: String) = Authenticated.async{ implicit request =>
    Order.getById(id).flatMap {{
      case Some(order) if order.idUser == request.user.id =>
        Order.check(id).map {{
          case true => Ok(msgOk("checked"))
          case false => BadRequest(msgErr("error"))
        }}
      case Some(order) => Future(BadRequest(msgErr("Permission error")))
      case _ => Future(BadRequest(msgErr("Not found order")))
    }}
  }
}
