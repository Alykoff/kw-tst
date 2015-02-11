package controllers

import java.util.UUID

import models.{User, Users}
import play.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import views.util.formdata.LoginData

import scala.util.Try

object SecureController extends Controller {
  val AUTH_TOKEN_HEADER = "X-AUTH-TOKEN"
  val AUTH_TOKEN = "authToken"

  case class AuthenticatedRequest(user: User, request: Request[AnyContent]) extends WrappedRequest(request)

  def Authenticated(f: AuthenticatedRequest => Result) = {
    Action { request =>
      val token = request.headers.get(AUTH_TOKEN_HEADER)
      Logger.debug(s"token: ${token}")
      val user = token.flatMap(uuid =>
        getUserByToken(uuid).flatMap(Users.getById)
      )
//      val user1 = request.body.asFormUrlEncoded.get.get("user").head
//      val user = request.session.get(Application.SESSION_KEY).flatMap(uuid => {
//        getUserId(uuid).flatMap(Users.getById)
//      })

      user.map { user =>
        f(AuthenticatedRequest(user, request))
      }.getOrElse(Unauthorized)
    }
  }

  def getUserByToken(uuid: String): Option[Long] = {
    Logger.info(s"uuid: $uuid, getUserId: ${Cache.getAs[Long](uuid)}")
    Cache.getAs[Long](uuid)
  }

  def createToken = UUID.randomUUID.toString

  def createSecuritySession(uuid: String, loginData: LoginData): Unit = {
    val userId = Users.getByEmail(loginData.email).map(_.id)
    userId.foreach(Cache.set(uuid, _))
  }

  def delSecuritySession[A](implicit request: Request[A]): Unit = {
    val uuid = request.session.get(AUTH_TOKEN)
    Try (Cache.remove(uuid.get))
  }

//  case class Token(value: String)
//
//  val tokenForm = Form(
//    mapping(
//      "token" -> nonEmptyText
//    )(Token.apply)(Token.unapply)
//  )
}
