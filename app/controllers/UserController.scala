package controllers

import models.User
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import utils.Utils.{msgErr, msgOk}

import play.api.libs.functional.syntax._


import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object UserController extends Controller {
  case class ThinUser(name: String, email: String, password: String)
  implicit val userReads: Reads[ThinUser] = (
    (__ \ "email").read[String](email keepAnd minLength[String](5)) ~
    (__ \ "name").read[String](minLength[String](2)) ~
    (__ \ "password").read[String](minLength[String](2))
  )(ThinUser.apply _)

  def create = Action.async(BodyParsers.parse.json) { implicit request =>
    def handleValidRequest(thinUser: ThinUser) = {
      val newUser = User("", thinUser.email, thinUser.name, thinUser.password)
      User.create(newUser) map {{
        case None => BadRequest(msgErr("Don't create or save."))
        case _ => Ok(msgOk("User '" + newUser.name + "' saved."))
      }}
    }
    Future {
      request.body.validate[ThinUser]
    } flatMap { userResult =>
      userResult.fold(
        errors => Future(BadRequest(msgErr("Bad data"))),
        thinUser => handleValidRequest(thinUser)
      )
    }
  }
}
