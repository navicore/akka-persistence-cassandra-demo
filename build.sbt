name := "AkkaPersistenceCassandraDemo"

fork := true
javaOptions in test ++= Seq(
  "-Xms512M", "-Xmx2048M",
  "-XX:MaxPermSize=2048M",
  "-XX:+CMSClassUnloadingEnabled"
)

parallelExecution in test := false

version := "1.0"

ensimeScalaVersion in ThisBuild := "2.12.4"
scalaVersion := "2.12.4"
val akkaVersion = "2.5.6"
val akkaHttpVersion = "10.0.10"
val phantomVersion = "2.15.3"

libraryDependencies ++=
  Seq(
    "ch.megard" %% "akka-http-cors" % "0.2.1",

    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe" % "config" % "1.2.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

    // if mongodb api instead, use...
    //"com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "2.0.4",
    //"org.mongodb" %% "casbah" % "3.1.1",

    "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.58",
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "0.58" % "test",

    "org.json4s" %% "json4s-native" % "3.5.3",
    "com.github.nscala-time" %% "nscala-time" % "2.16.0",

    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )

dependencyOverrides ++= Set(
  "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)

mainClass in assembly := Some("onextent.akka.persistence.demo.Main")
assemblyJarName in assembly := "AkkaPersistenceCassandraDemo.jar"

assemblyMergeStrategy in assembly := {
  case PathList("reference.conf") => MergeStrategy.concat
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

