package octostore.location

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import octostore.listing.{Listing, ListingId, RequestTrackListing}

import scala.collection.mutable

case class LocationId(value: String) extends AnyVal {
  override def toString = value
}

object Location {
  def props(locationId: LocationId): Props = Props(new Location(locationId))
}

class Location(locationId: LocationId) extends Actor with ActorLogging {

  val listingIdToActor = mutable.Map.empty[ListingId, ActorRef]
  val actorToListingId = mutable.Map.empty[ActorRef, ListingId]

  override def preStart(): Unit = log.info("Location actor {} started", locationId)

  override def postStop(): Unit = log.info("Location actor {} stopped", locationId)

  override def receive = {
    case trackMsg@RequestTrackListing(_, `locationId`, _) =>
      log.debug("RequestTrackListing {} for {}-{} received", trackMsg.requestId, trackMsg.locationId, trackMsg.listingId)
      listingIdToActor.get(trackMsg.listingId) match {
        case Some(listingActor) =>
          log.debug("Listing actor found {}, forwarding message", listingActor)
          listingActor forward trackMsg
        case None =>
          log.info("Creating Listing actor for {}", trackMsg.listingId)
          val listingActor = context.actorOf(Listing.props(trackMsg.listingId, trackMsg.locationId), s"listing-${trackMsg.listingId}")
          context.watch(listingActor)
          listingIdToActor.put(trackMsg.listingId, listingActor)
          actorToListingId.put(listingActor, trackMsg.listingId)
          listingActor forward trackMsg
      }

    case RequestTrackListing(id, requestedLocationId, _) =>
      log.warning("Ignoring TrackListing request {} for {}. This actor is responsible for {}",
        id, requestedLocationId, locationId)

    case RequestListings(requestId) =>
      log.debug("Listing list requested {}", requestId)
      sender() ! ReplyListings(requestId, listingIdToActor.keySet)

    case Terminated(listingActor) =>
      val listingId = actorToListingId(listingActor)
      log.info("Listing actor for {} has been terminated", listingId)
      actorToListingId.remove(listingActor)
      listingIdToActor.remove(listingId)
  }
}
