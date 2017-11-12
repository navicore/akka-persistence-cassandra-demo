package onextent.akka.persistence.demo.actors

import akka.actor._
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.persistence.demo.actors.AssessmentService._
import onextent.akka.persistence.demo.models.assessments._

object AssessmentService {
  def props(implicit timeout: Timeout) = Props(new AssessmentService)
  def name = "AssessmentService"

  final case class GetByName(name: String)
  final case class DeleteByName(name: String)
}

class AssessmentService(implicit timeout: Timeout)
    extends Actor
    with PersistentActor
    with LazyLogging {

  val conf: Config = ConfigFactory.load()
  val snapShotInterval: Int = conf.getInt("main.snapShotInterval")
  override def persistenceId: String = conf.getString("main.assessmentPersistenceId")

  var state: Map[String, Assessment] = Map[String, Assessment]()

  override def receiveRecover: Receive = {

    case assessment: Assessment =>
      state = state + (assessment.name -> assessment)

    case deleteCmd: DeleteByName =>
      state = state - deleteCmd.name

    case SnapshotOffer(_, snapshot: Map[String, Assessment] @unchecked) => state = snapshot

  }

  override def receiveCommand: Receive = {

    case assessment: Assessment =>
      state = state + (assessment.name -> assessment)
      persistAsync(assessment) { a =>
        {
          sender() ! Some(a)
          if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
            saveSnapshot(state)
        }
      }

    case deleteCmd: DeleteByName =>
      state = state - deleteCmd.name
      persistAsync(deleteCmd) { _ =>
        {
          sender() ! Some(deleteCmd)
          if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
            saveSnapshot(state)
        }
      }

    case GetByName(name) => sender() ! state.get(name)

  }

}
