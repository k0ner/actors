package octostore.location

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import octostore.listing._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._


class StoreManagerSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val firstStoreId = StoreId("first-store")
  val secondStoreId = StoreId("second-store")
  val id = TimeUuid(0)
  val firstListing = ListingId("1")
  val secondListing = ListingId("2")
  val wrongStore = StoreId("wrongStore")

  val sut = system.actorOf(StoreManager.props())

  "Store actor" should {

    "be able to register a store actor" in {
      sut ! RequestTrackListing(id, firstStoreId, firstListing)
      expectMsg(ListingRegistered(id))

      val firstListingActor = lastSender

      sut ! RequestTrackListing(id, secondStoreId, secondListing)
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
      sut ! RequestTrackListing(id, firstStoreId, firstListing)
      expectMsg(ListingRegistered(id))

      val firstListingActor = lastSender

      sut ! RequestTrackListing(id, firstStoreId, firstListing)
      expectMsg(ListingRegistered(id))

      val secondListingActor = lastSender
      firstListingActor should ===(secondListingActor)
    }

    "be able to list active stores" in {
      sut ! RequestTrackListing(id, firstStoreId, firstListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestTrackListing(id, secondStoreId, secondListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestStoreList(id)
      expectMsg(ReplyStoreList(id, Set(firstStoreId, secondStoreId)))
    }

    "be able to list active listings after one shuts down" in {
      sut ! RequestTrackListing(id, firstStoreId, firstListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestTrackListing(id, secondStoreId, secondListing)
      expectMsg(ListingRegistered(id))

      sut ! RequestStoreList(id)
      expectMsg(ReplyStoreList(id, Set(firstStoreId, secondStoreId)))

      // just to get actor ref
      system.actorSelection(s"akka://testSystem/user/*/store-$firstStoreId") ! RequestListings(id)
      expectMsgClass(classOf[ReplyListings])
      val toShutDown = lastSender

      watch(toShutDown)
      toShutDown ! PoisonPill
      expectTerminated(toShutDown, 500.millis)

      awaitAssert {
        sut ! RequestStoreList(id)
        expectMsg(ReplyStoreList(id, Set(secondStoreId)))
      }
    }
  }
}
