package octostore.store

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import octostore.listing._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._


class StoreSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val storeId = StoreId("store")
  val id = TimeUuid(0)
  val firstItem = ItemId("1")
  val secondItem = ItemId("2")
  val wrongStore = StoreId("wrongStore")

  val sut = system.actorOf(Store.props(storeId))

  "Store actor" should {

    "be able to register an item actor" in {
      sut ! RequestTrackLocation(id, storeId, firstItem)
      expectMsg(LocationRegistered(id))

      val firstItemActor = lastSender

      sut ! RequestTrackLocation(id, storeId, secondItem)
      expectMsg(LocationRegistered(id))

      val secondItemActor = lastSender
      firstItemActor should !==(secondItemActor)

      // Check that item actors are working
      firstItemActor ! RecordInventory(id, 1)
      expectMsg(InventoryRecorded(id))

      secondItemActor ! RecordInventory(id, 2)
      expectMsg(InventoryRecorded(id))
    }

    "ignore request for wrong storeId" in {
      sut ! RequestTrackLocation(id, wrongStore, firstItem)
      expectNoMessage(500.millis)
    }

    "return same actor for same itemId" in {
      sut ! RequestTrackLocation(id, storeId, firstItem)
      expectMsg(LocationRegistered(id))

      val firstItemActor = lastSender

      sut ! RequestTrackLocation(id, storeId, firstItem)
      expectMsg(LocationRegistered(id))

      val secondItemActor = lastSender
      firstItemActor should ===(secondItemActor)
    }

    "be able to list active items" in {
      sut ! RequestTrackLocation(id, storeId, firstItem)
      expectMsg(LocationRegistered(id))

      sut ! RequestTrackLocation(id, storeId, secondItem)
      expectMsg(LocationRegistered(id))

      sut ! RequestItemList(id)
      expectMsg(ReplyItemList(id, Set(firstItem, secondItem)))
    }

    "be able to list active items after one shuts down" in {
      sut ! RequestTrackLocation(id, storeId, firstItem)
      expectMsg(LocationRegistered(id))
      val toShutDown = lastSender

      sut ! RequestTrackLocation(id, storeId, secondItem)
      expectMsg(LocationRegistered(id))

      sut ! RequestItemList(id)
      expectMsg(ReplyItemList(id, Set(firstItem, secondItem)))

      watch(toShutDown)
      toShutDown ! PoisonPill
      expectTerminated(toShutDown, 500.millis)

      awaitAssert {
        sut ! RequestItemList(id)
        expectMsg(ReplyItemList(id, Set(secondItem)))
      }
    }
  }
}
