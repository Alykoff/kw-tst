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

import play.api.libs.functional.syntax._

object StoreController extends Controller {
  val msgErrSave = Json.obj("status" -> "err", "message" -> "Don't create or save.")
  def msgSuccessSavedUser(name: String) = Json.obj("status" -> "ok", "message" -> ("User '" + name + "' saved."))
  val itemsInPage = 30

  implicit val productReads: Reads[ProductType] = (
    (__ \ "id_product").read[Long] and
    (__ \ "name").read[String] and
    (__ \ "cost").read[Double]
  )(ProductType.apply _)

  implicit val productWrites: Writes[ProductType] = (
    (__ \ "id_product").write[Long] and
    (__ \ "name").write[String] and
    (__ \ "cost").write[Double]
  )(unlift(ProductType.unapply))

  implicit val storeItemWriters: Writes[ProductPosition] = (
    (__ \ "id_product").write[Long] and
    (__ \ "counts").write[Long]
  )(unlift(ProductPosition.unapply))

  implicit val storeWriters: Writes[Store] = (__ \ "store_items").lazyWrite(Writes.seq[ProductPosition](storeItemWriters)).contramap(unlift(Store.unapply))
//  .lazyWrite(Writes.traversableWrites[Creature](creatureWrites))

  def get(page: Long) = Action{ implicit request =>
    val store = Store.get(page * itemsInPage, page * (itemsInPage + 1))
    store match {
      case Success(Some(value)) => Ok(Json.obj("status" -> "ok", "result" -> Json.toJson(value).toString))
      case _ => BadRequest(Json.obj("status" -> "err", "message" -> "bad data."))
    }
  }
}
