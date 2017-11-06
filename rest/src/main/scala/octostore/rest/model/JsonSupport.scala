package octostore.rest.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsString, JsValue, RootJsonWriter}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val symptomFormat = jsonFormat1(Symptom)

  implicit object HealthyJsonWriter extends RootJsonWriter[Healthy] {
    def write(healthy: Healthy): JsValue =
      JsObject(healthy.name ->
        JsObject(
          "status" -> JsString("healthy")
        ))
  }

  implicit object UnhealthyJsonWriter extends RootJsonWriter[Unhealthy] {
    def write(unhealthy: Unhealthy): JsValue =
      JsObject(unhealthy.name ->
        JsObject(
          "status" -> JsString("unhealthy"),
          "symptoms" -> JsArray(unhealthy.symptoms.map(_.description).map(JsString(_)).toVector)
        )
      )
  }

  implicit object DiagnosisJsonWriter extends RootJsonWriter[Diagnosis] {
    def write(diagnosis: Diagnosis) = diagnosis match {
      case healthy: Healthy => HealthyJsonWriter.write(healthy)
      case unhealthy: Unhealthy => UnhealthyJsonWriter.write(unhealthy)
    }
  }

  implicit object HealthReportWriter extends RootJsonWriter[HealthReport] {
    def write(healthReport: HealthReport) =
      JsObject(
        "versionNumber" -> JsString(healthReport.versionNumber),
        "healthChecks" -> JsArray(healthReport.healthChecks.map(DiagnosisJsonWriter.write(_)).toVector)
      )
  }

}


