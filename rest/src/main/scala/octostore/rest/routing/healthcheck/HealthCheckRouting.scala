package octostore.rest.routing.healthcheck

import akka.http.scaladsl.server.Directives._
import octostore.rest.model.{HealthCheck, HealthReport, JsonSupport}

case class HealthCheckRouting(applicationVersion: String, checks: Seq[HealthCheck]) extends JsonSupport {

  val routes = {
    path("health") {
      get {
        complete {
          HealthReport(applicationVersion, checks.map(_.checkHealth()))
        }
      }
    }
  }
}
