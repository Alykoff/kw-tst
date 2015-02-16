package controllers

import models.User
import play.Logger
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import utils.Utils.{getUserByToken, AUTH_TOKEN_HEADER}

import scala.concurrent.Future

case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

object Authenticated extends Controller { authSelf =>

  private[Authenticated] def unwrapResult[A](opUser: Future[Option[User]], f: AuthenticatedRequest[A] => Result)(implicit request: Request[A]): Future[Result] = {
    opUser map { case user => user match {
      case Some(us) => f(AuthenticatedRequest(us, request))
      case _ => Unauthorized
    }}
  }

  private[Authenticated] def unwrapFutureResult[A](opUser: Future[Option[User]], f: AuthenticatedRequest[A] => Future[Result])(implicit request: Request[A]): Future[Result] = {
    opUser flatMap { case user => user match {
      case Some(us) => f(AuthenticatedRequest(us, request))
      case _ => Future(Unauthorized)
    }}
  }

  private[Authenticated] def base[A](implicit request: Request[A]) = {
    Future {
      val token = request.headers.get(AUTH_TOKEN_HEADER)//Some("1")
      val user = token.flatMap(getUserByToken).map(User.getById) match {
        case None => Future.successful(Option.empty[User])
        case Some(x) => x
      }
      user
    }
  }

  def apply[A](bodyParser: BodyParser[A])(f: AuthenticatedRequest[A] => Result) =
    Action.async(bodyParser) { implicit request =>
      base.flatMap{unwrapResult(_, f)}
    }

  def apply(f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] =
    authSelf[AnyContent](BodyParsers.parse.anyContent)(f)

  object async { asyncSelf =>
    def apply[A](bodyParser: BodyParser[A])(f: AuthenticatedRequest[A] => Future[Result]) =
      Action.async(bodyParser) { implicit request =>
        base flatMap { unwrapFutureResult(_, f)}
      }

    def apply(f: AuthenticatedRequest[AnyContent] => Future[Result]): Action[AnyContent] =
      asyncSelf(BodyParsers.parse.anyContent)(f)
  }
}