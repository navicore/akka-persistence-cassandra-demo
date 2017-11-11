package onextent.akka.persistence.demo.models.assessments.db

import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoints}
import com.typesafe.config.ConfigFactory

object Connector {
  private val config = ConfigFactory.load()

  private val contactPoint1 = config.getString("cassandra.contactPoint1")
  private val keyspace = config.getString("cassandra.keyspace")
  private val username = config.getString("cassandra.username")
  private val password = config.getString("cassandra.password")

  lazy val connector: CassandraConnection = ContactPoints(Seq(contactPoint1))
    .withClusterBuilder(_.withCredentials(username, password))
    .keySpace(keyspace)


}
