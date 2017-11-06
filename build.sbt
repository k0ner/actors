val namePrefix = "octostore"

name := s"${Build.namePrefix}-root"

version := "0.0.2"

scalaVersion := "2.12.3"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

lazy val engine = project.
  settings(libraryDependencies ++= Dependencies.engineDependencies)

lazy val rest = project.
  settings(libraryDependencies ++= Dependencies.restDependencies)

lazy val root = (project in file(".")).
  aggregate(engine, rest)
