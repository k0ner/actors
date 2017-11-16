package octostore.rest.model

import octostore.tools.RichString._
import org.json4s.native.Serialization.{read, write}
import org.scalatest.{Matchers, WordSpec}

class UnhealthySerializerSpec extends WordSpec with Matchers with JsonSupport {

  val sampleName = "sample-name"
  val dbDown = "database-down"
  val linkDown = "link-down"
  val symptoms = Set(Symptom(dbDown), Symptom(linkDown))

  "UnhealthySerializer" should {

    "serialize unhealthy object" in {

      val json = write(Unhealthy(sampleName, symptoms))

      json should ===(
        s"""
           |{
           |  "service": "$sampleName",
           |  "status": "unhealthy",
           |  "symptoms": [ "$dbDown", "$linkDown" ]
           |}
         """.stripWhitespaces
      )
    }

    "deserialize json to Unhealthy object" in {

      val inputJson =
        s"""
           |{
           |  "service": "$sampleName",
           |  "status": "unhealthy",
           |  "symptoms": [ "$dbDown", "$linkDown"]
           |}
         """.stripMargin

      val unhealthy = read[Unhealthy](inputJson)

      unhealthy.name should ===(sampleName)
      unhealthy.symptoms should ===(symptoms)
    }
  }
}
