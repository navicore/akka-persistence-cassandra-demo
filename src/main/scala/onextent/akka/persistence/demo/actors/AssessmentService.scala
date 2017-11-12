package onextent.akka.persistence.demo.actors

import java.util.UUID

import akka.pattern.ask
import akka.actor._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.persistence.demo.actors.AssessmentService._
import onextent.akka.persistence.demo.models.assessments._

import scala.concurrent.{ExecutionContextExecutor, Future}

object AssessmentService {
  def props(implicit timeout: Timeout) = Props(new AssessmentService)
  def name = "AssessmentService"

  final case class GetByName(name: String, limit: Int)
  final case class GetById(id: UUID)
  final case class Delete(id: UUID)
}
class AssessmentService(implicit timeout: Timeout)
    extends Actor
    with LazyLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  val exampleActor: ActorRef = context.actorOf( Props(new ExamplePersistentActor), "examplePersistentActor")
  val af: Future[Any] = exampleActor ask "a"
  af.onComplete(f => logger.debug(s"ejs a got ${f.getOrElse("none")}"))
  exampleActor ask "b"
  exampleActor ask "c"
  exampleActor ask "d"
  val bf = exampleActor ask "state"
  bf.onComplete(f => logger.debug(s"ejs state got ${f.getOrElse("none")}"))
  exampleActor ask "e"

  override def receive: PartialFunction[Any, Unit] = {

    case Delete(id)             =>

    case GetById(id)            =>

    case GetByName(name, limit) =>

    // store
    case Assessment(name, value, _, _) =>

    case _                             => sender() ! "huh?"

  }

}
