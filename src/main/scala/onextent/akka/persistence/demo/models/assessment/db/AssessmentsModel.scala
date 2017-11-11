package onextent.akka.persistence.demo.models.assessments.db

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.{Date, UUID}

import com.outworkers.phantom.dsl._
import onextent.akka.persistence.demo.models.assessments.Assessment

import scala.concurrent.Future

abstract class AssessmentsModel extends Table[ConcreteAssessmentsModel, Assessment] {

  override def tableName: String = "assessments"

  // String because TimedUUIDs are bad bad bad
  object id extends Col[String] with PartitionKey {
    override lazy val name = "assessment_id"
  }

  object name extends Col[String]

  object value extends Col[Double]

  object datetime extends DateColumn {
    override lazy val name = "assessment_datetime"
  }

  override def fromRow(r: Row): Assessment = Assessment(name(r), value(r), Some(UUID.fromString(id(r))), Some( ZonedDateTime.ofInstant(datetime(r).toInstant, ZoneOffset.UTC) ))
}

abstract class ConcreteAssessmentsModel extends AssessmentsModel with RootConnector {

  def getByAssessmentId(id: UUID): Future[Option[Assessment]] = {
    select
      .where(_.id eqs id.toString)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

  def store(assessment: Assessment): Future[ResultSet] = {
    insert
      .value(_.id, assessment.id.get.toString)
      .value(_.datetime, new Date(assessment.datetime.get.toInstant.toEpochMilli))
      .value(_.name, assessment.name)
      .value(_.value, assessment.value)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .ifNotExists
      .future()
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete
      .where(_.id eqs id.toString)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
}

