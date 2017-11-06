package octostore.location

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import octostore.listing._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._


class LocationSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val locationId = LocationId("locationId")
  val id = TimeUuid(0)
  val firstListing = ListingId("1")
  val secondListing = ListingId("2")
  val wrongLocation = LocationId("wrongLocationId")

  val sut = system.actorOf(Location.props(locationId))

  "Location actor" should {

    "be able to register an listing actor" in {
      sut ! RequestTrackListing(id, locationId, firstListing)
      expectMsg(ListingRegistered(id))

      val firstListingActor = lastSender

      sut ! RequestTrackListing(id, locationId, secondListing)
      expectMsg(ListingRegistered(id))

      val secondListingActor = lastSender
      firstListingActor should !==(secondListingActor)

      // Check that listing actors are working
      firstListingActor ! RecordInventory(id, 1)
      expectMsg(InventoryRecorded(id))

      secondListingActor ! RecordInventory(id, 2)
      expectMsg(InventoryRecorded(id))
    }

    "ignore request for wrong locationId" in {
      sut ! RequestTrackListing(id, wrongLocation, firstListing)
      expectNoMessage(500.millis)
    }

    "return same actor for same listingId" in {
      sut ! RequestTrackListing(id, locationId, firstListing)
      expectMsg(ListingRegistered(id))

      val firstListingActor = lastSender

      sut ! RequestTrackListing(id, locationId, firstListing)
      expectMsg(ListingRegistered(id))

      val secondListingActor = lastSender
      firstListingActor should ===(secondListingActor)
    }

    "be able to list active listings" in {
      sut ! RequestTrackListing(id, locationId, firstListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestTrackListing(id, locationId, secondListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestListings(id)
      expectMsg(ReplyListings(id, Set(firstListing, secondListing)))
    }

    "be able to list active listings after one shuts down" in {
      sut ! RequestTrackListing(id, locationId, firstListing)
      expectMsg(ListingRegistered(id))
      val toShutDown = lastSender

      sut ! RequestTrackListing(id, locationId, secondListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestListings(id)
      expectMsg(ReplyListings(id, Set(firstListing, secondListing)))

      watch(toShutDown)
      toShutDown ! PoisonPill
      expectTerminated(toShutDown, 500.millis)

      awaitAssert {
        sut ! RequestListings(id)
        expectMsg(ReplyListings(id, Set(secondListing)))
      }
    }
  }
}
