import sbt._

object Dependencies {

  val AkkaVersion = "2.5.6"
  val AkkaHttpVersion = "10.0.10"
  val Json4sVersion = "3.5.3"
  val ScalaTestVersion = "3.0.4"

  val engineDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
    "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test",
    "com.gilt" %% "gfc-timeuuid" % "0.0.8" // is it needed ??
  )

  val restDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.18.1",
    "org.json4s" %% "json4s-native" % Json4sVersion,
    "org.json4s" %% "json4s-ext" % Json4sVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
  )

}
