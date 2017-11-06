package octostore.rest.routing.healthcheck

import octostore.rest.model.{HealthCheck, Healthy}

object ExampleHealth extends HealthCheck {
  override def checkHealth() = Healthy("example")
}
