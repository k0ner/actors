package inventory.availability

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.gilt.timeuuid.TimeUuid
import inventory.item.{ReadInventory, RespondInventory}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.duration._

class StoreQuerySpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  val id = TimeUuid(0)

  val firstItemId = "firstItemId"
  val secondItemId = "secondItemId"

  "Store query" should {
    "return availability value for active items" in new TestSupport {

      firstItem.expectMsg(ReadInventory(id))
      secondItem.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstItem.ref)
      sut.tell(RespondInventory(id, 2), secondItem.ref)

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstItemId -> Availability(1),
            secondItemId -> Availability(2))))
    }

    //TODO [k0] when NoAvailability is introduced
    "return NoAvailability for items with no readings" ignore new TestSupport {

      firstItem.expectMsg(ReadInventory(id))
      secondItem.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstItem.ref)
      sut.tell(RespondInventory(id, 2), secondItem.ref)

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstItemId -> Availability(1),
            secondItemId -> Availability(2))))
    }

    "return ItemNotAvailable if item stops before answering" in new TestSupport {

      firstItem.expectMsg(ReadInventory(id))
      secondItem.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstItem.ref)
      secondItem.ref ! PoisonPill

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstItemId -> Availability(1),
            secondItemId -> ItemNotAvailable)))

    }

    "return availability reading even if item stops after answering" in new TestSupport {

      firstItem.expectMsg(ReadInventory(id))
      secondItem.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstItem.ref)
      sut.tell(RespondInventory(id, 2), secondItem.ref)
      secondItem.ref ! PoisonPill

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstItemId -> Availability(1),
            secondItemId -> Availability(2))))
    }

    "return ItemTimedOut if item does not answer in time" in new TestSupport {

      firstItem.expectMsg(ReadInventory(id))
      secondItem.expectMsg(ReadInventory(id))

      sut.tell(RespondInventory(id, 1), firstItem.ref)

      expectMsg(
        RespondAllAvailabilities(
          requestId = id,
          availabilities = Map(
            firstItemId -> Availability(1),
            secondItemId -> ItemTimedOut)))
    }
  }

  class TestSupport {

    val firstItem = TestProbe()
    val secondItem = TestProbe()
    val sut = system.actorOf(
      StoreQuery.props(
        actorToItemId = Map(firstItem.ref -> firstItemId, secondItem.ref -> secondItemId),
        requestId = id,
        requester = self,
        timeout = 1.seconds))
  }

}
