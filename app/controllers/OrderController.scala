package controllers

import models.ProductType
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import controllers.SecureController.Authenticated

object OrderController extends Controller {


  implicit val orderItemReads: Reads[ProductType] = (
    (__ \ "id_product").read[Long] and
      (__ \ "name").read[String] and
      (__ \ "cost").read[Double]
    )(ProductType.apply _)

  def create() = Authenticated { implicit request =>


    Ok(request.user.email)
  }

  def edit(id: Long) = Action {
    Ok("")
  }

  def get(id: Long) = Action {
    Ok("")
  }

  def order = Action {implicit request =>
    Ok(views.html.order())
  }

  def o(id: Long, ud: Long) = Action {
    Ok("")
  }

}
