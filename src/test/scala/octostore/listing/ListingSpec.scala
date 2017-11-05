package octostore.listing

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import octostore.store.StoreId
import org.scalatest._

import scala.concurrent.duration._

class ListingSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val listing = ListingId("11")
  val store = StoreId("32")
  val wrongListing = ListingId("wrongListing")
  val wrongStore = StoreId("wrongStore")

  val sut = system.actorOf(Listing.props(listing, store))

  val id = TimeUuid(42)

  "Listing actor" should {

    "reply with empty reading if no inventory is known" in {
      sut ! ReadInventory(id)
      expectMsg(RespondInventory(id, 0))
    }

    "reply with latest temperature reading" in {
      sut ! RecordInventory(id, 10)
      expectMsg(InventoryRecorded(id))

      sut ! ReadInventory(id)
      expectMsg(RespondInventory(id, 10))

      sut ! RecordInventory(id, 20)
      expectMsg(InventoryRecorded(id))

      sut ! ReadInventory(id)
      expectMsg(RespondInventory(id, 30))
    }

    "reply to registration request" in {
      sut ! RequestTrackListing(id, store, listing)
      expectMsg(ListingRegistered(id))
      lastSender should ===(sut)
    }

    "ignore wrong registration request" should {
      "wrong listing" in {
        sut ! RequestTrackListing(id, store, wrongListing)
        expectNoMessage(500.millis)
      }

      "wrong store" in {
        sut ! RequestTrackListing(id, wrongStore, listing)
        expectNoMessage(500.millis)
      }

      "wrong listing and store" in {
        sut ! RequestTrackListing(id, wrongStore, wrongListing)
        expectNoMessage(500.millis)
      }
    }
  }

}
