package onextent.akka.persistence.demo.actors

import akka.actor._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait ActorServiceSupport extends Actor {

  def createAssessmentActor(actorId: String): Unit = {
    def notFound(): Unit = context.actorOf(AssessmentActor.props(actorId), actorId)
    context
      .child(actorId)
      .fold(notFound())(_ => {})
  }
  def forwardIfExists[T: TypeTag](query: T, actorId: String)(
      implicit tag: ClassTag[T]): Unit = {
    def notFound(): Unit = sender() ! None
    context
      .child(actorId)
      .fold(notFound())(_ forward query)
  }
  def forward[T: TypeTag](query: T, actorId: String)(
      implicit tag: ClassTag[T]): Unit = {
    def notFound(): Unit =
      context.actorOf(AssessmentActor.props(actorId), actorId) forward query
    context
      .child(actorId)
      .fold(notFound())(_ forward query)
  }
  //ugh - more correct to send a delete and then a poison pill in case it gets created again
  def stopActor[T: TypeTag](query: T, actorId: String)(
      implicit tag: ClassTag[T]): Unit = {
    def notFound(): Unit = sender() ! None
    context
      .child(actorId)
      .fold(notFound())(context.stop)
  }

}
