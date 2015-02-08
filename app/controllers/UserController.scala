package controllers

import play.api.libs.concurrent.Promise
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 *
 * @author alykoff
 *         07.02.2015
 */
object UserController extends Controller {
  def create = Action.async { request =>
    val res = Future {
      Ok("hello").as("application/json")
    }
    res.recoverWith {
      case e: RuntimeException =>
        Future(InternalServerError("error"))
    }
    Future.successful(Ok("hello"))
    val timeoutFuture = play.api.libs.concurrent.Promise.timeout("Oops", 1.second)

//    Promise.timeout(Ok("hello"), 5.second)
    // timeout
    Future.firstCompletedOf(Seq(res, timeoutFuture)).map {
      case i: Result => Ok("Got result: " + i)
      case t: String => InternalServerError(t)
    }
  }

  def get(id: Long) = Action.async {
    Future(Ok(""))
  }

}
