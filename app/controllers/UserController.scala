package controllers

import models.{ThinUser, User}
import play.api.mvc._
import play.api.libs.json.JsError
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation.ValidationError

import play.api.libs.functional.syntax._


import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object UserController extends Controller {
  implicit val userReads: Reads[ThinUser] = (
    (__ \ "email").read[String](email keepAnd minLength[String](5)) ~
    (__ \ "name").read[String](minLength[String](2)) ~
    (__ \ "password").read[String](minLength[String](2))
  )(ThinUser.apply _)

  val msgErrSaveUser = Json.obj("status" -> "err", "message" -> "Don't create or save.")
  def msgErrParse(errors: Seq[(JsPath, Seq[ValidationError])]) = Json.obj("status" -> "err", "message" -> JsError.toFlatJson(errors))
  def msgErr(errorMsg: String) = Json.obj("status" -> "err", "message" -> errorMsg)
  def msgSuccessSavedUser(name: String) = Json.obj("status" -> "ok", "message" -> ("User '" + name + "' saved."))
  
  def create = Action(BodyParsers.parse.json) { implicit request =>
    val userResult = request.body.validate[ThinUser]
    userResult.fold (
      errors => BadRequest(msgErrParse(errors)),
      thinUser => User.create(thinUser) match {
        case None => BadRequest(msgErrSaveUser)
        case _ => Ok(msgSuccessSavedUser(thinUser.name))
      }
    )
  }

  def get(id: Long) = Action.async {
    Future(Ok(""))
  }
}
