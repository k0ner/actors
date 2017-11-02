package inventory.store

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import com.gilt.timeuuid.TimeUuid
import inventory.item._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._


class StoreManagerSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val firstStoreId = StoreId("first-store")
  val secondStoreId = StoreId("second-store")
  val id = TimeUuid(0)
  val firstItem = ItemId("1")
  val secondItem = ItemId("2")
  val wrongStore = StoreId("wrongStore")

  val sut = system.actorOf(StoreManager.props())

  "Store actor" should {

    "be able to register a store actor" in {
      sut ! RequestTrackLocation(id, firstStoreId, firstItem)
      expectMsg(LocationRegistered(id))

      val firstItemActor = lastSender

      sut ! RequestTrackLocation(id, secondStoreId, secondItem)
      expectMsg(LocationRegistered(id))

      val secondItemActor = lastSender
      firstItemActor should !==(secondItemActor)

      // Check that item actors are working
      firstItemActor ! RecordInventory(id, 1)
      expectMsg(InventoryRecorded(id))

      secondItemActor ! RecordInventory(id, 2)
      expectMsg(InventoryRecorded(id))
    }

    "return same actor for same itemId" in {
      sut ! RequestTrackLocation(id, firstStoreId, firstItem)
      expectMsg(LocationRegistered(id))

      val firstItemActor = lastSender

      sut ! RequestTrackLocation(id, firstStoreId, firstItem)
      expectMsg(LocationRegistered(id))

      val secondItemActor = lastSender
      firstItemActor should ===(secondItemActor)
    }

    "be able to list active stores" in {
      sut ! RequestTrackLocation(id, firstStoreId, firstItem)
      expectMsg(LocationRegistered(id))

      sut ! RequestTrackLocation(id, secondStoreId, secondItem)
      expectMsg(LocationRegistered(id))

      sut ! RequestStoreList(id)
      expectMsg(ReplyStoreList(id, Set(firstStoreId, secondStoreId)))
    }

    "be able to list active items after one shuts down" in {
      sut ! RequestTrackLocation(id, firstStoreId, firstItem)
      expectMsg(LocationRegistered(id))

      sut ! RequestTrackLocation(id, secondStoreId, secondItem)
      expectMsg(LocationRegistered(id))

      sut ! RequestStoreList(id)
      expectMsg(ReplyStoreList(id, Set(firstStoreId, secondStoreId)))

      // just to get actor ref
      system.actorSelection(s"akka://testSystem/user/*/store-$firstStoreId") ! RequestItemList(id)
      expectMsgClass(classOf[ReplyItemList])
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
