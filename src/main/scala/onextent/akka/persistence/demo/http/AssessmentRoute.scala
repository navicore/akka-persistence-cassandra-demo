package onextent.akka.persistence.demo.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.persistence.demo.actors.AssessmentService._
import onextent.akka.persistence.demo.models.assessments._
import spray.json._

import scala.concurrent.Future

object AssessmentRoute
    extends AssessmentJsonSupport
    with LazyLogging
    with Directives
    with ErrorSupport {

  val defaultLimit = 1

  def apply(service: ActorRef): Route =
    logRequest(urlpath) {
      handleErrors {
        cors(corsSettings) {

          path(urlpath / "assessment" / JavaUUID) { id =>
            get {
              val f: Future[Any] = service ask GetById(id)
              onSuccess(f) { (r: Any) =>
                {
                  r match {
                    case Some(assessment: Assessment) =>
                      complete(HttpEntity(ContentTypes.`application/json`,
                                          assessment.toJson.prettyPrint))
                    case _ =>
                      complete(StatusCodes.NotFound)
                  }
                }
              }
            } ~
              delete {
                val f: Future[Any] = service ask Delete(id)
                onSuccess(f) { (r: Any) =>
                  {
                    r match {
                      case Delete(deletedId) =>
                        complete(StatusCodes.OK,
                                 s"assessment $deletedId deleted")
                      case _ =>
                        complete(StatusCodes.NotFound)
                    }
                  }
                }
              }

          } ~
            path(urlpath / "assessment") {
              get {
                parameters('name.as[String], 'limit.as[Int] ? defaultLimit) {
                  (name, limit) =>
                    {
                      val f: Future[Any] = service ask GetByName(name, limit)
                      onSuccess(f) { (r: Any) =>
                        {
                          r match {
                            case assessments: List[Assessment @unchecked] =>
                              complete(
                                HttpEntity(ContentTypes.`application/json`,
                                           assessments.toJson.prettyPrint))
                            case _ =>
                              complete(StatusCodes.NotFound)
                          }
                        }
                      }
                    }
                }
              } ~
                post {
                  decodeRequest {
                    entity(as[Assessment]) { assessment =>
                      val f: Future[Any] = service ask assessment
                      onSuccess(f) { (r: Any) =>
                        {
                          r match {
                            case Some(assessment: Assessment) =>
                              complete(
                                HttpEntity(ContentTypes.`application/json`,
                                           assessment.toJson.prettyPrint))
                            case _ =>
                              complete(StatusCodes.NotFound)
                          }
                        }
                      }
                    }
                  }
                }
            }
        }
      }
    }

}
