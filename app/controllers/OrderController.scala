package controllers

import models._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Action, Controller}
import controllers.SecureController.Authenticated
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import scala.collection.immutable.TreeSet

import scala.collection.immutable.TreeMap

object OrderController extends Controller {


  implicit val positionReads: Reads[Position] = (
    (__ \ "id_item").read[Long] ~
    (__ \ "count").read[Long](min(0L))
  )(Position.apply _)

  implicit val positionWrites: Writes[Position] = (
    (__ \ "id_item").write[Long] ~
    (__ \ "count").write[Long]
  )(unlift(Position.unapply))

//  implicit val orderReads: Reads[Order] = (
//    (__ \ "id_order").read[Long] ~
//    (__ \ "id_user").read[Long] ~
//    (__ \ "status").read[Boolean] ~
//    (__ \ "positions").lazyRead(Reads.set[Position](positionReads))
//  )(Order.apply _)

  implicit val orderWrites: Writes[Order] = (
    (__ \ "id_order").write[Long] ~
    (__ \ "id_user").write[Long] ~
    (__ \ "status").write[Boolean] ~
    (__ \ "positions").lazyWrite(Writes.set[Position](positionWrites))
  ) (unlift(Order.unapply))

  case class EditOrder(orderId: Long, items: Set[Position])

  implicit val editOrderReads: Reads[EditOrder] = (
    (__ \ "id_order").read[Long] ~
    (__ \ "edit_items").lazyRead(Reads.set[Position](positionReads))
  ) (EditOrder.apply _)
  implicit val editOrderWrites: Writes[EditOrder] = (
    (__ \ "id_order").write[Long] ~
    (__ \ "edit_items").lazyWrite(Writes.set[Position](positionWrites))
  ) (unlift(EditOrder.unapply))

  case class CreateOrder(items: Set[Position])
  implicit val createOrderReads: Reads[CreateOrder] =
    (__ \ "edit_items").lazyRead(Reads.set[Position](positionReads)).map(CreateOrder.apply)
  implicit val createOrderWrites: Writes[CreateOrder] =
    (__ \ "edit_items").lazyWrite(Writes.set[Position](positionWrites)).contramap(unlift(CreateOrder.unapply))

  case class CheckOrder(idOrder: Long)
  implicit val checkOrderReads: Reads[CheckOrder] = (__ \ "id_order").read[Long].map(CheckOrder.apply)

//  def create() = Authenticated { implicit request =>
//
////    val re =
////      (__ \ "id_order").write[Long] and
////        (__ \ "id_user").write[Long] and
////        (__ \ "positions").lazyWrite(Writes.seq[Position](positionWrites)
//////    re(unlift(Position.unapply))
//
//
//    Ok(request.user.email)
//  }

  def msgSuccess(msg: String) = Json.obj("status" -> "ok", "message" -> msg)

  def edit(id: Long) = Authenticated(BodyParsers.parse.json) { implicit request =>
    val editOrderResult = request.body.validate[EditOrder]
    editOrderResult.fold(
      error => Ok("Bad data input"),
      editOrder => {
        val user = request.user
        val rawOrder = Order.getById(editOrder.orderId).filter(_.idUser == user.id).filter(!_.status)
        rawOrder match {
          case None => Ok("order not found")
          case Some(order) =>
            Logger.info(s"editOrder: $editOrder")
            val newOrder = Order.edit(order, editOrder)
            Logger.info(s"editOrder: ${Order.orders}")
            Ok(msgSuccess("saved"))
        }
      }
    )
  }

  def get(id: Long) = Authenticated { implicit request =>
    Logger.info(Json.toJson(
      EditOrder(1L, Set(Position(1, 4), Position(2, 4), Position(3, 4), Position(6, 4)))
    ).toString)

    Order.getById(id) match {
      case Some(order) if order.idUser == request.user.id =>
        Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(order)))
      case Some(order) =>
        BadRequest(UserController.msgErr("Permission error"))
      case _ =>
        BadRequest(UserController.msgErr("Not found order"))
    }
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

  def check(id: Long) = Authenticated { implicit request =>
//    val checkOrderInput = request.body.validate[CheckOrder]
//    checkOrderInput.fold(
//      error => BadRequest("bad data"),
//      checkOrder => {
        Order.getById(id) match {
          case Some(order) if order.idUser == request.user.id =>
            Logger.info(s"order: ${order.positions}")
            Order.check(id)
            Ok(msgSuccess("checked"))
          case Some(order) => BadRequest(UserController.msgErr("Permission error"))
          case _ => BadRequest(UserController.msgErr("Not found order"))
        }
//      }
//    )
  }

}
