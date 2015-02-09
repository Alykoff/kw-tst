package controllers

import models.{User, Users}
import play.Logger
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

object SecureController extends Controller {
  case class AuthenticatedRequest(user: User, request: Request[AnyContent]) extends WrappedRequest(request)

  def Authenticated(f: AuthenticatedRequest => Result) = {
    Action { request =>
      val user = request.session.get(Application.SESSION_KEY).flatMap(uuid => {
        val userId = getUserId(uuid).getOrElse(-1L)
        Logger.info(s"userId: {userId}")
        Users.getById(userId)
      })
      
      user.map { user =>
        f(AuthenticatedRequest(user, request))
      }.getOrElse(Unauthorized)
    }
  }

  def getUserId(uuid: String): Option[Long] = Cache.getAs[Long](uuid)

  def saveUserSession(uuid: String, idUser: Long) = {

  }
}
