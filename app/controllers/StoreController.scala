package controllers

import models._
import play.api.libs.json.JsError
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import play.api.libs.json.OWrites
import play.api.data.validation.ValidationError
import scala.util.Success
import play.api.libs.json.Json.JsValueWrapper

import play.api.libs.functional.syntax._

object StoreController extends Controller {
  val errStatus = "status" -> "err"
  val msgErrSave = Json.obj("status" -> "err", "message" -> "Don't create or save.")
  def msgSuccessSavedUser(name: String) = Json.obj("status" -> "ok", "message" -> ("User '" + name + "' saved."))
  val itemsInPage = 30

  implicit val productReads: Reads[ProductType] = (
    (__ \ "id_product").read[Long] ~
    (__ \ "name").read[String] ~
    (__ \ "cost").read[Double]
  )(ProductType.apply _)

  implicit val productWrites: Writes[ProductType] = (
    (__ \ "id_product").write[Long] ~
    (__ \ "name").write[String] ~
    (__ \ "cost").write[Double]
  )(unlift(ProductType.unapply))

  implicit val storeItemWriters: Writes[Position] = (
    (__ \ "id_product").write[Long] ~
    (__ \ "counts").write[Long]
  )(unlift(Position.unapply))

  implicit val storeWriters: Writes[Store] =
    (__ \ "positions").lazyWrite(Writes.seq[Position](storeItemWriters)).contramap(unlift(Store.unapply))
//  .lazyWrite(Writes.traversableWrites[Creature](creatureWrites))

  def get(page: Long) = Action{ implicit request =>
    val store = Store.get(page * itemsInPage, page * (itemsInPage + 1))
    store match {
      case Success(value) => Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(value).toString))
      case _ => BadRequest(Json.obj("status" -> "err", "message" -> "bad data."))
    }
  }

  def get = Action(BodyParsers.parse.json) { implicit request =>
    val storeFromJson = request.body.validate[Store]
    storeFromJson.fold(
      error => BadRequest,
      store => Ok("")
    )
  }
}
