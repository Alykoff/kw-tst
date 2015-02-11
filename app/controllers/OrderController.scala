package controllers

import models._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Action, Controller}
import controllers.SecureController.{Authenticated}
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._

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

  implicit val orderReads: Reads[Order] = (
    (__ \ "id_order").read[Long] ~
    (__ \ "id_user").read[Long] ~
    (__ \ "status").read[Boolean] ~
    (__ \ "positions").lazyRead(Reads.list[Position](positionReads))
  )(Order.apply _)

  implicit val orderWrites: Writes[Order] = (
    (__ \ "id_order").write[Long] ~
    (__ \ "id_user").write[Long] ~
    (__ \ "status").write[Boolean] ~
    (__ \ "positions").lazyWrite(Writes.list[Position](positionWrites))
  ) (unlift(Order.unapply))

  case class EditOrder(orderId: Long, items: Set[Position])

  implicit val editOrder: Reads[EditOrder] = (
    (__ \ "id_order").read[Long] ~
    (__ \ "edit_items").lazyRead(Reads.set[Position](positionReads))
  ) (EditOrder.apply _)


//  implicit val orderReads: Reads[ProductType] = (
//    (__ \ "id_item").read[Long]
//  )(ProductType.apply _)

  def create() = Authenticated { implicit request =>

//    val re =
//      (__ \ "id_order").write[Long] and
//        (__ \ "id_user").write[Long] and
//        (__ \ "positions").lazyWrite(Writes.seq[Position](positionWrites)
////    re(unlift(Position.unapply))


    Ok(request.user.email)
  }

  def edit(id: Long) = Authenticated(BodyParsers.parse.json) { implicit request =>
    val editOrderResult = request.body.validate[EditOrder]
    editOrderResult.fold(
      error => ???,
      editOrder => {
        val user = request.user
        val rawOrder = Order.getById(editOrder.orderId).filter(_.idUser == user.id).filter(!_.status)
        rawOrder match {
          case None => ???
          case Some(order) =>
            Logger.info(s"editOrder: $editOrder")
            val newOrder = ??? //order.getUpdatedOrder(editOrder.items)
            Logger.info(s"newOrder: $newOrder")
            newOrder
        }
      }
    )
    Ok("")
  }

  def get(id: Long) = Authenticated { implicit request =>
    Order.getById(id) match {
      case Some(order) if order.idUser == request.user.id =>
        Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(order)))
      case Some(order) =>
        BadRequest(UserController.msgErr("Permission error"))
      case _ =>
        BadRequest(UserController.msgErr("Not found order"))
    }
  }

  def order = Action {implicit request =>
    Ok(views.html.order())
  }

}
