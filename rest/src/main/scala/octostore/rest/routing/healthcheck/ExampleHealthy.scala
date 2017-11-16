package octostore.rest.routing.healthcheck

import octostore.rest.model.{HealthCheck, Healthy, Symptom, Unhealthy}

object ExampleHealthy extends HealthCheck {
  override def checkHealth() = Healthy("example")
}

object ExampleUnhealthy extends HealthCheck {
  override def checkHealth() = Unhealthy("example-unhealthy", Set(Symptom("baaaad")))
}
