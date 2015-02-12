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
      def unwrapResult(opUser: Future[Option[User]]): Future[Result] = {
        opUser flatMap { case user =>
          user match {
            case Some(us) => f(AuthenticatedRequest(us, request))
            case _ => Future(Unauthorized)
          }
        }
      }
      Future {
        val token = Some("1") //request.headers.get(AUTH_TOKEN_HEADER)
        Logger.debug(s"token: ${token}")
        val user = token.flatMap(getUserByToken).map(User.getById) match {
          case None => Future.successful(Option.empty[User])
          case Some(x) => x
        }
        user
      }.flatMap(unwrapResult)
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
