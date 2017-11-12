package onextent.akka.persistence.demo.actors

import akka.persistence._
import com.typesafe.scalalogging.LazyLogging

case class Cmd(data: String)
case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)
  def size: Int = events.length
  override def toString: String = events.reverse.toString
}


class ExamplePersistentActor extends PersistentActor with LazyLogging {

  override def persistenceId = "persistExample_id_1"

  override def receiveRecover: Receive = {
    case d: String => logger.debug(s"ejs recovering: ${d.getClass.getName}")
    case RecoveryCompleted => logger.debug("ejs done recovery")
    case SnapshotOffer(_, snapshot: String) =>
      logger.debug(s"ejs snapshot recoover: $snapshot")
      state = snapshot
    case x => logger.debug("ejs UNKNOWN: " + x)
  }

  val snapShotInterval = 3
  var state: String = ""

  override def receiveCommand: Receive = {
    case "state" => sender() ! state
    case c: String => {
      state = c
      persistAsync(s"evt-$c") {
        e => sender() ! e
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) saveSnapshot(state)
      }
    }
  }
}

/*
class ExamplePersistentActor extends PersistentActor with LazyLogging {
  override def persistenceId = "sample-id-1"

  var state = ExampleState()

  def updateState(event: Evt): Unit =
    state = state.updated(event)

  def numEvents: Int =
    state.size

  val receiveRecover: Receive = {
    case evt: Evt                                 => updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) => state = snapshot
  }

  val snapShotInterval = 1000
  val receiveCommand: Receive = {
    case Cmd(data) =>
      persist(Evt(s"$data-$numEvents")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
          saveSnapshot(state)
      }
    case "print" => logger.debug(s"$state")
  }

}
*/
