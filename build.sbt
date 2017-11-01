name := "actors"

version := "0.1"

scalaVersion := "2.12.3"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.12" % "2.5.6"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test"
libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.6" % "test"
libraryDependencies += "com.gilt" %% "gfc-timeuuid" % "0.0.8" // is it needed ??

