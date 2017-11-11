package onextent.akka.persistence.demo.actors

import java.util.UUID

import akka.actor._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.persistence.demo.actors.AssessmentService._
import onextent.akka.persistence.demo.models.assessments._
import onextent.akka.persistence.demo.models.assessments.db._

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
    with CassandraDatabase
    with LazyLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  import com.outworkers.phantom.dsl._
  database.create()

  def delete(assessment: Assessment): Future[ResultSet] = {
    for {
      _ <- database.assessmentsModel.deleteById(assessment.id.get)
      byName <- database.assessmentsByNamesModel
        .deleteByNameAndDatetime(assessment.name, assessment.datetime.get)
    } yield byName
  }

  def store(assessment: Assessment): Future[ResultSet] = {
    for {
      _ <- database.assessmentsModel.store(assessment)
      byName <- database.assessmentsByNamesModel.store(assessment)
    } yield byName
  }

  override def receive: PartialFunction[Any, Unit] = {

    case Delete(id) =>
      val sdr = sender()

      database.assessmentsModel
        .getByAssessmentId(id)
        .onComplete(r => {
          if (r.get.isEmpty) sdr ! None
          else delete(r.get.head).onComplete(_ => sdr ! Delete(id))
        })

    case GetById(id) =>
      val sdr = sender()
      database.assessmentsModel
        .getByAssessmentId(id)
        .onComplete(
          r =>
            if (r.get.isEmpty)
              sdr ! None
            else
              sdr ! Some(r.get.head))

    case GetByName(name, limit) =>
      val sdr = sender()
      database.assessmentsByNamesModel
        .getByName(name)
        .onComplete(r => sdr ! r.get.slice(0, limit))

    case Assessment(name, value, _, _) =>
      val sdr = sender()
      val newAssessment = Assessment(name, value)
      store(newAssessment).onComplete(_ => sdr ! Some(newAssessment))

    case _ => sender() ! "huh?"
  }

}
