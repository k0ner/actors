package octostore.availability

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.gilt.timeuuid.TimeUuid
import octostore.listing.{ReadInventory, RespondInventory}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.duration._

class LocationQuerySpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  val id = TimeUuid(0)

  val firstListingId = "firstListingId"
  val secondListingId = "secondListingId"

  "Location query" should {
    "return availability value for active listings" in new TestSupport {

      firstListing.expectMsg(ReadInventory(id))
      secondListing.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstListing.ref)
      sut.tell(RespondInventory(id, 2), secondListing.ref)

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstListingId -> Availability(1),
            secondListingId -> Availability(2))))
    }

    //TODO [k0] when NoAvailability is introduced
    "return NoAvailability for listings with no readings" ignore new TestSupport {

      firstListing.expectMsg(ReadInventory(id))
      secondListing.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstListing.ref)
      sut.tell(RespondInventory(id, 2), secondListing.ref)

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstListingId -> Availability(1),
            secondListingId -> Availability(2))))
    }

    "return ListingNotAvailable if listing stops before answering" in new TestSupport {

      firstListing.expectMsg(ReadInventory(id))
      secondListing.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstListing.ref)
      secondListing.ref ! PoisonPill

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstListingId -> Availability(1),
            secondListingId -> ListingNotReachable)))

    }

    "return availability reading even if listing stops after answering" in new TestSupport {

      firstListing.expectMsg(ReadInventory(id))
      secondListing.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstListing.ref)
      sut.tell(RespondInventory(id, 2), secondListing.ref)
      secondListing.ref ! PoisonPill

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstListingId -> Availability(1),
            secondListingId -> Availability(2))))
    }

    "return ListingTimedOut if listing does not answer in time" in new TestSupport {

      firstListing.expectMsg(ReadInventory(id))
      secondListing.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstListing.ref)

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstListingId -> Availability(1),
            secondListingId -> ListingTimedOut)))
    }
  }

  class TestSupport {

    val firstListing = TestProbe()
    val secondListing = TestProbe()
    val sut = system.actorOf(
      LocationQuery.props(
        actorToListingId = Map(firstListing.ref -> firstListingId, secondListing.ref -> secondListingId),
        requestId = id,
        requester = self,
        timeout = 1.seconds))
  }

}
