package controllers

import models.{ThinUser, User, Users}
import play.api.libs.json.JsError
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError

import play.api.libs.functional.syntax._

object StoreController extends Controller {

  implicit val userReads: Reads[ThinUser] = (
    (__ \ "email").read[String](email keepAnd minLength[String](5)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "password").read[String](minLength[String](2))
    )(ThinUser.apply _)

  def get(page: Long) = Action(BodyParsers.parse.json) { implicit request =>

    Ok("")
  }
}
