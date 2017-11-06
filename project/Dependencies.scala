import sbt._

object Dependencies {

  val akkaVersion = "2.5.6"

  val engineDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.gilt" %% "gfc-timeuuid" % "0.0.8" // is it needed ??
  )

  val restDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.10",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10"
  )

}
