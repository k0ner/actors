package octostore.item

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import octostore.store.StoreId
import org.scalatest._

import scala.concurrent.duration._

class ItemSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val item = ItemId("11")
  val store = StoreId("32")
  val wrongItem = ItemId("wrongItem")
  val wrongStore = StoreId("wrongStore")

  val sut = system.actorOf(Item.props(item, store))

  val id = TimeUuid(42)

  "Item actor" should {

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
      sut ! RequestTrackLocation(id, store, item)
      expectMsg(LocationRegistered(id))
      lastSender should ===(sut)
    }

    "ignore wrong registration request" should {
      "wrong item" in {
        sut ! RequestTrackLocation(id, store, wrongItem)
        expectNoMessage(500.millis)
      }

      "wrong store" in {
        sut ! RequestTrackLocation(id, wrongStore, item)
        expectNoMessage(500.millis)
      }

      "wrong item and store" in {
        sut ! RequestTrackLocation(id, wrongStore, wrongItem)
        expectNoMessage(500.millis)
      }
    }
  }

}
