package octostore.rest.model

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JsonAST.{JArray, JField, JObject}
import org.json4s.JsonDSL._
import org.json4s.native.Serialization
import org.json4s.{CustomSerializer, JString, NoTypeHints, native}

trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization
  implicit val formats = Serialization.formats(NoTypeHints) + new HealthySerializer + new UnhealthySerializer

}

class HealthySerializer extends CustomSerializer[Healthy](_ => ( {
  case JObject(
  JField("service", JString(name)) ::
    JField("status", JString("healthy")) :: Nil) => Healthy(name)
}, {
  case healthy: Healthy =>
    ("service" -> JString(healthy.name)) ~
      ("status" -> "healthy")

}))

class UnhealthySerializer extends CustomSerializer[Unhealthy](_ => ( {
  case JObject(
  JField("service", JString(name)) ::
    JField("status", JString("unhealthy")) ::
    JField("symptoms", JArray(symptoms)) :: Nil) =>
    Unhealthy(name, symptoms.map {
      case x: JString => Symptom(x.values)
      case _ => throw new RuntimeException
    }.toSet)
}, {
  case unhealthy: Unhealthy =>
    ("service" -> JString(unhealthy.name)) ~
      ("status" -> "unhealthy") ~
        ("symptoms" -> JArray(unhealthy.symptoms.toList.map(s => JString(s.description))))
}))
