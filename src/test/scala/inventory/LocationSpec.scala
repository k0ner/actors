package inventory

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import inventory.queries.{InventoryRecorded, ReadInventory, RecordInventory, RespondInventory}
import org.scalatest._

import scala.concurrent.duration._

class LocationSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val item = ItemId("11")
  val store = StoreId("32")
  val wrongItem = ItemId("wrongItem")
  val wrongStore = StoreId("wrongStore")

  val sut = system.actorOf(Location.props(item, store))

  val uuid = TimeUuid(42)

  "Location" should {

    "reply with empty reading if no inventory is known" in {
      sut ! ReadInventory(uuid)
      expectMsg(RespondInventory(uuid, 0))
    }

    "reply with latest temperature reading" in {
      sut ! RecordInventory(uuid, 10)
      expectMsg(InventoryRecorded(uuid))

      sut ! ReadInventory(uuid)
      expectMsg(RespondInventory(uuid, 10))

      sut ! RecordInventory(uuid, 20)
      expectMsg(InventoryRecorded(uuid))

      sut ! ReadInventory(uuid)
      expectMsg(RespondInventory(uuid, 30))
    }

    "reply to registration request" in {
      sut ! RequestTrackLocation(store, item)
      expectMsg(LocationRegistered)
      lastSender should ===(sut)
    }

    "ignore wrong registration request" should {
      "wrong item" in {
        sut ! RequestTrackLocation(store, wrongItem)
        expectNoMessage(500.millis)
      }

      "wrong store" in {
        sut ! RequestTrackLocation(wrongStore, item)
        expectNoMessage(500.millis)
      }

      "wrong item and store" in {
        sut ! RequestTrackLocation(wrongStore, wrongItem)
        expectNoMessage(500.millis)
      }
    }
  }

}
