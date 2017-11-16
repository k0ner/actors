import sbt._

object Dependencies {

  val AkkaVersion = "2.5.6"
  val Json4sVersion = "3.5.3"

  val engineDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test",
    "com.gilt" %% "gfc-timeuuid" % "0.0.8" // is it needed ??
  )

  val restDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.10",
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",
    "de.heikoseeberger" % "akka-http-json4s_2.12" % "1.18.1",
    "org.json4s" %% "json4s-native" % Json4sVersion,
    "org.json4s" %% "json4s-ext" % Json4sVersion,
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )

}
