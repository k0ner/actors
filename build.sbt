val namePrefix = "octostore"

name := s"${Build.namePrefix}-root"

version := "0.0.2"

scalaVersion := "2.12.3"

lazy val engine = project.
  settings(Common.settings: _*).
  settings(libraryDependencies ++= Dependencies.engineDependencies)

lazy val rest = project.
  settings(Common.settings: _*).
  settings(libraryDependencies ++= Dependencies.restDependencies)

lazy val root = (project in file(".")).
  settings(Common.settings: _*).
  settings(libraryDependencies ++= Dependencies.restDependencies).
  aggregate(engine, rest)
