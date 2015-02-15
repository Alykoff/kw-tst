package controllers

import models._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Controller}
import controllers.Authenticated
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object OrderController extends Controller {
  implicit val positionReads: Reads[Position] = (
    (__ \ "id_item").read[Long] ~
    (__ \ "count").read[Long](min(0L))
  )(Position.apply _)

  implicit val positionWrites: Writes[Position] = (
    (__ \ "id_item").write[Long] ~
    (__ \ "count").write[Long]
  )(unlift(Position.unapply))

  implicit val orderWrites: Writes[Order] = (
    (__ \ "id_order").write[String] ~
    (__ \ "id_user").write[String] ~
    (__ \ "status").write[Boolean] ~
    (__ \ "positions").lazyWrite(Writes.list[Position](positionWrites))
  ) (unlift(Order.unapply))

  case class EditOrder(orderId: String, items: Set[Position])

  implicit val editOrderReads: Reads[EditOrder] = (
    (__ \ "id_order").read[String] ~
    (__ \ "edit_items").lazyRead(Reads.set[Position](positionReads))
  ) (EditOrder.apply _)
  implicit val editOrderWrites: Writes[EditOrder] = (
    (__ \ "id_order").write[String] ~
    (__ \ "edit_items").lazyWrite(Writes.set[Position](positionWrites))
  ) (unlift(EditOrder.unapply))

  case class CreateOrder(items: Set[Position])
  implicit val createOrderReads: Reads[CreateOrder] =
    (__ \ "edit_items").lazyRead(Reads.set[Position](positionReads)).map(CreateOrder.apply)
  implicit val createOrderWrites: Writes[CreateOrder] =
    (__ \ "edit_items").lazyWrite(Writes.set[Position](positionWrites)).contramap(unlift(CreateOrder.unapply))

  case class CheckOrder(idOrder: String)
  implicit val checkOrderReads: Reads[CheckOrder] = (__ \ "id_order").read[String].map(CheckOrder.apply)

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

  def get(id: String) = Authenticated { implicit request =>
    Logger.info(Json.toJson(
      EditOrder("1", Set(Position(1, 4), Position(2, 4), Position(3, 4), Position(6, 4)))
    ).toString)
    Ok("")
//    Order.getById(id) map{{
//      case Some(order) if order.idUser == request.user.id =>
//        Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(order)))
//      case Some(order) =>
//        BadRequest(UserController.msgErr("Permission error"))
//      case _ =>
//        BadRequest(UserController.msgErr("Not found order"))
//    }}
  }

  def create = Authenticated(BodyParsers.parse.json) {implicit request =>
    val editOrderResult = request.body.validate[CreateOrder]
    editOrderResult.fold(
      error => BadRequest("Bad data input"),
      createOrder => {
        val userId = request.user.id
        val newOrder = Order.create(createOrder.items, userId)
        Ok(msgSuccess("saved"))
      }
    )
  }

  def check(id: String) = Authenticated { implicit request => Ok("")
//    Order.getById(id) match {
//      case Some(order) if order.idUser == request.user.id =>
//        Logger.info(s"order: ${order.positions}")
//        Order.check(id)
//        Ok(msgSuccess("checked"))
//      case Some(order) => BadRequest(UserController.msgErr("Permission error"))
//      case _ => BadRequest(UserController.msgErr("Not found order"))
//    }
  }

}
