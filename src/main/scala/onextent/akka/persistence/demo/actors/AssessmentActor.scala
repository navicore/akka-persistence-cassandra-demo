package onextent.akka.persistence.demo.actors

import akka.actor._
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.persistence.demo.actors.AssessmentService.GetByName
import onextent.akka.persistence.demo.models.assessments._

object AssessmentActor {
  def props(name: String) = Props(new AssessmentActor(name))
}

class AssessmentActor(name: String)
    extends Actor
    with PersistentActor
    with LazyLogging {

  val conf: Config = ConfigFactory.load()
  val snapShotInterval: Int = conf.getInt("main.snapShotInterval")

  override def persistenceId: String =
    conf.getString("main.assessmentPersistenceId") + "_" + name

  var state: Option[Assessment] = None

  override def receiveRecover: Receive = {

    case assessment: Assessment =>
      state = Some(assessment)

    case SnapshotOffer(_, snapshot: Assessment) => state = Some(snapshot)

  }

  override def receiveCommand: Receive = {

    case assessment: Assessment =>
      state = Some(assessment)
      persistAsync(assessment) { _ =>
        {
          sender() ! state
          if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
            saveSnapshot(state)
        }
      }

    case GetByName(_) => sender() ! state

  }

}
