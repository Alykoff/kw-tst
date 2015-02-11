package controllers

import models.{Store, Position, ProductType, Order}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import controllers.SecureController.Authenticated
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._

object OrderController extends Controller {

  implicit val positionReads: Reads[Position] = (
    (__ \ "id_item").read[Long] ~
    (__ \ "count").read[Long]
  )(Position.apply _)

  implicit val positionWrites: Writes[Position] = (
    (__ \ "id_item").write[Long] ~
    (__ \ "count").write[Long]
  )(unlift(Position.unapply))

  implicit val orderReads: Reads[Order] = (
    (__ \ "id_order").read[Long] ~
    (__ \ "id_user").read[Long] ~
    (__ \ "positions").lazyRead(Reads.seq[Position](positionReads))
  )(Order.apply _)

  implicit val orderWrites: Writes[Order] = (
    (__ \ "id_order").write[Long] ~
    (__ \ "id_user").write[Long] ~
    (__ \ "positions").lazyWrite(Writes.seq[Position](positionWrites))
  ) (unlift(Order.unapply))


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

  def edit(id: Long) = Authenticated { implicit request =>


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

  def o(id: Long, ud: Long) = Action {
    Ok("")
  }

}
