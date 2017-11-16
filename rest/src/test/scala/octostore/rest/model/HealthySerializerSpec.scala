package octostore.rest.model

import octostore.tools.RichString._
import org.json4s.native.Serialization.{read, write}
import org.scalatest.{Matchers, WordSpec}

class HealthySerializerSpec extends WordSpec with Matchers with JsonSupport {

  val sampleName = "sample-name"

  "HealthySerializer" should {

    "serialize healthy object" in {

      val json = write(Healthy(sampleName))

      json should ===(
        s"""
           |{
           |  "service": "$sampleName",
           |  "status": "healthy"
           |}
         """.stripWhitespaces
      )
    }

    "deserialize json to Healthy object" in {

      val inputJson =
        s"""
           |{
           |  "service": "$sampleName",
           |  "status": "healthy"
           |}
         """.stripMargin

      val healthy = read[Healthy](inputJson)

      healthy.name should ===(sampleName)
    }
  }
}
