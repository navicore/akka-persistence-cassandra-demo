package onextent.akka.persistence.demo.actors

import java.util.UUID

import akka.actor._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.persistence.demo.actors.AssessmentService._
import onextent.akka.persistence.demo.models.assessments._

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

  override def receive: PartialFunction[Any, Unit] = {

    case Delete(id)             =>

    case GetById(id)            =>

    case GetByName(name, limit) =>

    // store
    case Assessment(name, value, _, _) =>

    case _                             => sender() ! "huh?"

  }

}
