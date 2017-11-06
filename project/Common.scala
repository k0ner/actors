import sbt.Keys._
import sbt._

object Common {
  val appVersion = "0.0.2"

  lazy val copyDependencies = TaskKey[Unit]("copy-dependencies")

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    scalaVersion := "2.12.3",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"), //, "-Xmx2G"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
  )
}