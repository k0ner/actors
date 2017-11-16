package octostore.rest.routing.healthcheck

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import octostore.rest.model.{HealthCheck, Healthy}
import octostore.tools.RichString._
import org.scalatest.{Matchers, WordSpec}

class HealthCheckRoutingTest extends WordSpec with Matchers with ScalatestRouteTest {

  val sut = HealthCheckRouting("v1", Seq(SimpleCheck))

  "HealthCheck service" should {

    "return a health check status for GET requests to /health path" in {

      Get("/health") ~> sut.routes ~> check {
        status should ===(StatusCodes.OK)
        responseAs[String] should ===(
          s"""
             |{
             |  "versionNumber": "v1",
             |  "healthChecks": [
             |    {
             |      "service": "${SimpleCheck.name}",
             |      "status": "healthy"
             |    }
             |  ]
             |}
           """.stripWhitespaces
        )
      }
    }

    "return a MethodNotAllowed error for PUT requests to the /health path" in {

      Put("/health") ~> Route.seal(sut.routes) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }

  case object SimpleCheck extends HealthCheck {

    val name = "health"

    def checkHealth() = Healthy(name)
  }

}
