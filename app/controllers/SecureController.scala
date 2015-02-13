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
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object SecureController extends Controller {
  val AUTH_TOKEN_HEADER = "X-AUTH-TOKEN"
  val AUTH_TOKEN = "authToken"

  case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

  def Authenticated[A](bodyParser: BodyParser[A])(f: AuthenticatedRequest[A] => Result) = {
    Action.async(bodyParser) { implicit request =>
      def unwrapResult(opUser: Future[Option[User]]): Future[Result] = {
        Logger.info("in wrap methods")
        opUser map { case user =>
          user match {
            case Some(us) => {
              f(AuthenticatedRequest(us, request))
            }
            case _ => Unauthorized
          }
        }
      }
//      unwrapResult(Future(Some(User(1L, "b", "b@bb.bb", "111"))))
      Future {
        val token = Some("1") //request.headers.get(AUTH_TOKEN_HEADER)
        Logger.debug(s"token: ${token}")
        val user = token.flatMap(getUserByToken).map(User.getById) match {
          case None => Future.successful(Option.empty[User])
          case Some(x) => x
        }
        Logger.debug("in wrap methods")
        user
      } flatMap unwrapResult
    }
  }

  def Authenticated(f: AuthenticatedRequest[AnyContent] => Result) =
    Authenticated[AnyContent](BodyParsers.parse.anyContent)(f)

  def getUserByToken(uuid: String): Option[String] = {
    Logger.info(s"uuid: $uuid, getUserId: ${Cache.getAs[String](uuid)}")
    Cache.getAs[String](uuid)
    Some("1")
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
