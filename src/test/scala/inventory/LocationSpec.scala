package inventory

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import inventory.queries.{InventoryRecorded, ReadInventory, RecordInventory, RespondInventory}
import org.scalatest._

import scala.concurrent.duration._

class LocationSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val sut = system.actorOf(Location.props(item = ItemId("11"), store = StoreId("32")))
  val uuid = TimeUuid(42)

  "Location" should {

    "reply with empty reading if no inventory is known" in {
      sut ! ReadInventory(uuid)
      expectMsg(200.millis, RespondInventory(uuid, 0))
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
  }

}
