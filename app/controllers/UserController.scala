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


import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object UserController extends Controller {
  implicit val userReads: Reads[ThinUser] = (
    (__ \ "email").read[String](email keepAnd minLength[String](5)) and
    (__ \ "name").read[String](minLength[String](2)) and
    (__ \ "password").read[String](minLength[String](2))
  )(ThinUser.apply _)

//  implicit val userWrites: Writes[User] = (
//    (__ \ "id").write[Long] and
//    (__ \ "email").write[String] and
//    (__ \ "name").write[String] and
//    (__ \ "password").write[String]
//  )(unlift(User.unapply))

  val msgErrSaveUser = Json.obj("status" -> "err", "message" -> "Don't create or save.")
  def msgErrParseUser(errors: Seq[(JsPath, Seq[ValidationError])]) = Json.obj("status" -> "err", "message" -> JsError.toFlatJson(errors))
  def msgSuccessSavedUser(name: String) = Json.obj("status" -> "ok", "message" -> ("User '" + name + "' saved."))
  
  def create = Action(BodyParsers.parse.json) { implicit request =>
    val userResult = request.body.validate[ThinUser]
    userResult.fold (
      errors => {
        BadRequest(msgErrParseUser(errors))
      },
      thinUser => {
        val user = Users.create(thinUser.email, thinUser.name, thinUser.password)
        user match {
          case None => BadRequest(msgErrSaveUser)
          case _ => Ok(msgSuccessSavedUser(thinUser.name))
        }
      }
    )
//    val res = Future {
//      Ok("hello").as("application/json")
//    }
//    res.recoverWith {
//      case e: RuntimeException =>
//        Future(InternalServerError("error"))
//    }
//    Future.successful(Ok("hello"))
//    val timeoutFuture = play.api.libs.concurrent.Promise.timeout("Oops", 1.second)
//
////    Promise.timeout(Ok("hello"), 5.second)
//    // timeout
//    Future.firstCompletedOf(Seq(res, timeoutFuture)).map {
//      case i: Result => Ok("Got result: " + i)
//      case t: String => InternalServerError(t)
//    }
  }

  def get(id: Long) = Action.async {
    Future(Ok(""))
  }

}
