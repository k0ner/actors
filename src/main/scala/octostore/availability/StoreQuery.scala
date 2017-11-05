package octostore.availability

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import octostore.availability.StoreQuery.CollectionTimeout
import octostore.item.{ReadInventory, RespondInventory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object StoreQuery {

  case object CollectionTimeout

  def props(actorToItemId: Map[ActorRef, String],
            requestId: UUID,
            requester: ActorRef,
            timeout: FiniteDuration): Props =
    Props(new StoreQuery(actorToItemId, requestId, requester, timeout))
}

class StoreQuery(
                  actorToItemId: Map[ActorRef, String],
                  requestId: UUID,
                  requester: ActorRef,
                  timeout: FiniteDuration) extends Actor with ActorLogging {

  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout, self, CollectionTimeout)

  override def preStart(): Unit = {
    actorToItemId.keysIterator.foreach { itemActor =>
      context.watch(itemActor)
      itemActor ! ReadInventory(requestId)
    }
  }

  override def postStop(): Unit = queryTimeoutTimer.cancel()

  override def receive =
    waitingForReplies(Map.empty, actorToItemId.keySet)

  def waitingForReplies(repliesSoFar: Map[String, AvailabilityReading],
                        stillWaiting: Set[ActorRef]): Receive = {
    case RespondInventory(`requestId`, quantity) => receivedResponse(sender(), Availability(quantity), stillWaiting, repliesSoFar)

    case Terminated(_) => receivedResponse(sender(), ItemNotAvailable, stillWaiting, repliesSoFar)

    case CollectionTimeout =>
      val timedOutReplies =
        stillWaiting.map { itemActor =>
          val itemId = actorToItemId(itemActor)
          itemId -> ItemTimedOut
        }
      respond(repliesSoFar ++ timedOutReplies)
  }

  def respond(readings: Map[String, AvailabilityReading]) = {
    requester ! RespondAllAvailabilities(requestId, readings)
    context.stop(self)
  }

  def receivedResponse(itemActor: ActorRef,
                       reading: AvailabilityReading,
                       stillWaiting: Set[ActorRef],
                       repliesSoFar: Map[String, AvailabilityReading]): Unit = {

    context.unwatch(itemActor)
    val itemId = actorToItemId(itemActor)

    val newStillWaiting = stillWaiting - itemActor
    val newRepliesSoFar = repliesSoFar + (itemId -> reading)

    if (newStillWaiting.isEmpty) {
      respond(newRepliesSoFar)
    } else {
      context.become(waitingForReplies(newRepliesSoFar, newStillWaiting))
    }

  }
}
