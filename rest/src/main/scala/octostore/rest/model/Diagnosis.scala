package octostore.rest.model

case class Symptom(description: String)

sealed trait Diagnosis {
  def name: String
}

case class Healthy(name: String) extends Diagnosis

case class Unhealthy(name: String, symptoms: Set[Symptom]) extends Diagnosis

case class HealthReport(versionNumber: String, healthChecks: Seq[Diagnosis])

trait HealthCheck {
  def checkHealth(): Diagnosis
}
