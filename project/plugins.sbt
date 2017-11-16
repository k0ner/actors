resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

resolvers += "scoverage-bintray" at "https://dl.bintray.com/sksamuel/sbt-plugins"

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.2")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.11")
