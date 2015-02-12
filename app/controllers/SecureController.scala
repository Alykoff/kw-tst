package controllers

import java.util.UUID

import models.{User}
import play.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import views.util.formdata.LoginData

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object SecureController extends Controller {
  val AUTH_TOKEN_HEADER = "X-AUTH-TOKEN"
  val AUTH_TOKEN = "authToken"

  case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

  def Authenticated[A](bodyParser: BodyParser[A])(f: AuthenticatedRequest[A] => Future[Result]) = {
    Action.async(bodyParser) { implicit request =>
      Future {
        val token = Some("1") //request.headers.get(AUTH_TOKEN_HEADER)
        Logger.debug(s"token: ${token}")
        val user = token.flatMap(getUserByToken).map(User.getById) match {
          case None => Future.successful(Option.empty[User])
          case Some(x) => x
        }
        //        match {
        //          case Some(x) =>(x) match {
        //            case Success(y) =>
        //          }
        //          case _ => Future.successful(Option.empty[User])
        //        }}

//        val resp = user onComplete {
//          case Failure(_) => Unauthorized
//          case Success(optionUser) => optionUser match {
//            case Some(u) => f(AuthenticatedRequest(u, request))
//            case _ => Unauthorized
//          }
//        }

//
        user
      }.flatMap { u =>
        u map {
          f(AuthenticatedRequest(user, request))
        }.getOrElse(Unauthorized)
      }
    }
  }

  def Authenticated(f: AuthenticatedRequest[AnyContent] => Future[Result]) =
    Authenticated[AnyContent](BodyParsers.parse.anyContent)(f)

  def getUserByToken(uuid: String): Option[Long] = {
    Logger.info(s"uuid: $uuid, getUserId: ${Cache.getAs[Long](uuid)}")
    Cache.getAs[Long](uuid)
    Some(1L)
  }

  def createToken = UUID.randomUUID.toString

  def createSecuritySession(uuid: String, loginData: LoginData): Unit = {
    val userId = User.getByEmail(loginData.email).map(_.id)
    userId.foreach(Cache.set(uuid, _))
  }

  def delSecuritySession[A](implicit request: Request[A]): Unit = {
    val uuid = request.session.get(AUTH_TOKEN)
    Try (Cache.remove(uuid.get))
  }
}
