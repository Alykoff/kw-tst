package utils

import java.util.UUID

import models.User
import play.Logger
import play.api.mvc.Request
import views.util.formdata.LoginData
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import scala.util.Try

object Utils {
  val AUTH_TOKEN_HEADER = "X-AUTH-TOKEN"
  val AUTH_TOKEN = "authToken"

  def getUserByToken(uuid: String): Option[String] = {
    Logger.info(s"uuid: $uuid, getUserId: ${Cache.getAs[String](uuid)}")
    Cache.getAs[String](uuid)
    Some("1")
  }

  def createToken = UUID.randomUUID.toString

  def createSecuritySession(uuid: String, loginData: LoginData): Unit = {
    val userFutOpt = User.getByEmail(loginData.email)
    userFutOpt.map(_.foreach(Cache.set(uuid, _)))
  }

  def delSecuritySession[A](implicit request: Request[A]): Unit = {
    val uuid = request.session.get(AUTH_TOKEN)
    Try (Cache.remove(uuid.get))
  }
}
object Const {
  val BIGGER = 1
  val SMALLER = -1
  val EAQUALS = 0
}