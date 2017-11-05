package octostore.availability

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import octostore.availability.StoreQuery.CollectionTimeout
import octostore.listing.{ReadInventory, RespondInventory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object StoreQuery {

  case object CollectionTimeout

  def props(actorToListingId: Map[ActorRef, String],
            requestId: UUID,
            requester: ActorRef,
            timeout: FiniteDuration): Props =
    Props(new StoreQuery(actorToListingId, requestId, requester, timeout))
}

class StoreQuery(actorToListingId: Map[ActorRef, String],
                 requestId: UUID,
                 requester: ActorRef,
                 timeout: FiniteDuration) extends Actor with ActorLogging {

  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout, self, CollectionTimeout)

  override def preStart(): Unit = {
    actorToListingId.keysIterator.foreach { listingActor =>
      context.watch(listingActor)
      listingActor ! ReadInventory(requestId)
    }
  }

  override def postStop(): Unit = queryTimeoutTimer.cancel()

  override def receive =
    waitingForReplies(Map.empty, actorToListingId.keySet)

  def waitingForReplies(repliesSoFar: Map[String, AvailabilityReading],
                        stillWaiting: Set[ActorRef]): Receive = {
    case RespondInventory(`requestId`, quantity) => receivedResponse(sender(), Availability(quantity), stillWaiting, repliesSoFar)

    case Terminated(_) => receivedResponse(sender(), ListingNotReachable, stillWaiting, repliesSoFar)

    case CollectionTimeout =>
      val timedOutReplies =
        stillWaiting.map { listingActor =>
          val listingId = actorToListingId(listingActor)
          listingId -> ListingTimedOut
        }
      respond(repliesSoFar ++ timedOutReplies)
  }

  def respond(readings: Map[String, AvailabilityReading]) = {
    requester ! RespondAllAvailabilities(requestId, readings)
    context.stop(self)
  }

  def receivedResponse(listingActor: ActorRef,
                       reading: AvailabilityReading,
                       stillWaiting: Set[ActorRef],
                       repliesSoFar: Map[String, AvailabilityReading]): Unit = {

    context.unwatch(listingActor)
    val listingId = actorToListingId(listingActor)

    val newStillWaiting = stillWaiting - listingActor
    val newRepliesSoFar = repliesSoFar + (listingId -> reading)

    if (newStillWaiting.isEmpty) {
      respond(newRepliesSoFar)
    } else {
      context.become(waitingForReplies(newRepliesSoFar, newStillWaiting))
    }

  }
}
