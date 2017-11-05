package octostore.store

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import octostore.listing.{Listing, ListingId, RequestTrackListing}

import scala.collection.mutable

case class StoreId(value: String) extends AnyVal {
  override def toString = value
}

object Store {
  def props(storeId: StoreId): Props = Props(new Store(storeId))
}

class Store(storeId: StoreId) extends Actor with ActorLogging {

  val listingIdToActor = mutable.Map.empty[ListingId, ActorRef]
  val actorToListingId = mutable.Map.empty[ActorRef, ListingId]

  override def preStart(): Unit = log.info("Store {} started", storeId)

  override def postStop(): Unit = log.info("Store {} stopped", storeId)

  override def receive = {
    case trackMsg@RequestTrackListing(_, `storeId`, _) =>
      log.debug("RequestTrackListing {} for {}-{} received", trackMsg.requestId, trackMsg.store, trackMsg.listingId)
      listingIdToActor.get(trackMsg.listingId) match {
        case Some(listingActor) =>
          log.debug("Listing actor found {}, forwarding message", listingActor)
          listingActor forward trackMsg
        case None =>
          log.info("Creating Listing actor for {}", trackMsg.listingId)
          val listingActor = context.actorOf(Listing.props(trackMsg.listingId, trackMsg.store), s"listing-${trackMsg.listingId}")
          context.watch(listingActor)
          listingIdToActor.put(trackMsg.listingId, listingActor)
          actorToListingId.put(listingActor, trackMsg.listingId)
          listingActor forward trackMsg
      }

    case RequestTrackListing(id, requestedStoreId, _) =>
      log.warning("Ignoring TrackListing request {} for {}. This actor is responsible for {}",
        id, requestedStoreId, storeId)

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
