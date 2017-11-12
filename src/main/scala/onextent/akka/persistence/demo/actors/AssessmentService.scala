package onextent.akka.persistence.demo.actors

import akka.actor._
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.persistence.demo.actors.AssessmentService._
import onextent.akka.persistence.demo.models.assessments._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

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
  override def persistenceId: String =
    conf.getString("main.assessmentPersistenceId") + "_service"
  var state: List[String] = List[String]()

  //ugh
  private def forwardIfExists[T: TypeTag](query: T, actorId: String)(
      implicit tag: ClassTag[T]): Unit = {
    def notFound(): Unit = sender() ! None
    context
      .child(actorId)
      .fold(notFound())(_ forward query)
  }
  //ugh
  private def forward[T: TypeTag](query: T, actorId: String)(
      implicit tag: ClassTag[T]): Unit = {
    def notFound(): Unit =
      context.actorOf(AssessmentActor.props(actorId), actorId) forward query
    context
      .child(actorId)
      .fold(notFound())(_ forward query)
  }
  //ugh - more correct to send a delete and then a poison pill in case it gets created again
  private def stop[T: TypeTag](query: T, actorId: String)(
      implicit tag: ClassTag[T]): Unit = {
    def notFound(): Unit = sender() ! None
    context
      .child(actorId)
      .fold(notFound())(context.stop)
  }

  override def receiveRecover: Receive = {

    case assessment: Assessment =>
      state = assessment.name :: state
      forward(assessment, assessment.name)

    case deleteCmd: DeleteByName =>
      state = state.filter(n => n != deleteCmd.name)
      stop[DeleteByName](deleteCmd, deleteCmd.name)

    case SnapshotOffer(_, snapshot: List[String @unchecked]) => state = snapshot
      snapshot.foreach(actorId => {
        context.actorOf(AssessmentActor.props(actorId), actorId)
      })
      state = snapshot

  }

  override def receiveCommand: Receive = {

    case assessment: Assessment =>
      persistAsync(assessment) { _ =>
        {
          state = assessment.name :: state
          forward(assessment, assessment.name)
          if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
            saveSnapshot(state)
        }
      }

    case deleteCmd: DeleteByName =>
      if (state.contains(deleteCmd.name)) {
        state = state.filter(n => n != deleteCmd.name)
        stop[DeleteByName](deleteCmd, deleteCmd.name)
        persistAsync(deleteCmd) { _ =>
          {
            sender() ! Some(deleteCmd)
            if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
              saveSnapshot(state)
          }
        }
      } else {
        sender() ! None
      }

    case GetByName(name) => forwardIfExists(GetByName(name), name)

  }

}
