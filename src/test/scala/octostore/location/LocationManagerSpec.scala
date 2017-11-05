package octostore.location

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import octostore.listing._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._


class LocationManagerSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val firstLocationId = LocationId("firstLocationId")
  val secondLocationId = LocationId("secondLocationId")
  val id = TimeUuid(0)
  val firstListing = ListingId("1")
  val secondListing = ListingId("2")
  val wrongLocation = LocationId("wrongLocation")

  val sut = system.actorOf(LocationManager.props())

  "Location actor" should {

    "be able to register a location actor" in {
      sut ! RequestTrackListing(id, firstLocationId, firstListing)
      expectMsg(ListingRegistered(id))

      val firstListingActor = lastSender

      sut ! RequestTrackListing(id, secondLocationId, secondListing)
      expectMsg(ListingRegistered(id))

      val secondListingActor = lastSender
      firstListingActor should !==(secondListingActor)

      // Check that listing actors are working
      firstListingActor ! RecordInventory(id, 1)
      expectMsg(InventoryRecorded(id))

      secondListingActor ! RecordInventory(id, 2)
      expectMsg(InventoryRecorded(id))
    }

    "return same actor for same listingId" in {
      sut ! RequestTrackListing(id, firstLocationId, firstListing)
      expectMsg(ListingRegistered(id))

      val firstListingActor = lastSender

      sut ! RequestTrackListing(id, firstLocationId, firstListing)
      expectMsg(ListingRegistered(id))

      val secondListingActor = lastSender
      firstListingActor should ===(secondListingActor)
    }

    "be able to list active locations" in {
      sut ! RequestTrackListing(id, firstLocationId, firstListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestTrackListing(id, secondLocationId, secondListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestLocations(id)
      expectMsg(ReplyLocations(id, Set(firstLocationId, secondLocationId)))
    }

    "be able to list active listings after one shuts down" in {
      sut ! RequestTrackListing(id, firstLocationId, firstListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestTrackListing(id, secondLocationId, secondListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestLocations(id)
      expectMsg(ReplyLocations(id, Set(firstLocationId, secondLocationId)))

      // just to get actor ref
      system.actorSelection(s"akka://testSystem/user/*/location-$firstLocationId") ! RequestListings(id)
      expectMsgClass(classOf[ReplyListings])
      val toShutDown = lastSender

      watch(toShutDown)
      toShutDown ! PoisonPill
      expectTerminated(toShutDown, 500.millis)

      awaitAssert {
        sut ! RequestLocations(id)
        expectMsg(ReplyLocations(id, Set(secondLocationId)))
      }
    }
  }
}
