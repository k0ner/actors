import sbt._

object Dependencies {

  val engineDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" % "akka-actor_2.12" % "2.5.6",
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test",
    "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.6" % "test",
    "com.gilt" %% "gfc-timeuuid" % "0.0.8" // is it needed ??
  )

  val restDependencies: Seq[ModuleID] = Seq.empty

}
