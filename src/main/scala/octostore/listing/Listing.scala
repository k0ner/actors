package octostore.listing

import akka.actor.{Actor, ActorLogging, Props}
import octostore.location.StoreId

case class ListingId(value: String) extends AnyVal {
  override def toString = value
}

object Listing {
  def props(listingId: ListingId, store: StoreId): Props = Props(new Listing(listingId, store))
}

class Listing(listingId: ListingId, store: StoreId) extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("Listing actor {}-{} started", listingId, store)

  override def postStop(): Unit = log.info("Listing actor {}-{} stopped", listingId, store)

  override def receive = doReceive(0)

  def doReceive(availability: Int): Receive = {
    case RequestTrackListing(requestId, `store`, `listingId`) =>
      log.info("Registering listing {}-{} for requestId: {}", store, listingId, requestId)
      sender() ! ListingRegistered(requestId)

    case RequestTrackListing(id, requestedStore, requestedListingId) =>
      log.warning(s"Ignoring TrackListing request $id for $requestedStore-$requestedListingId." +
        s" This actor is responsible for ${this.store}-${this.listingId}.")

    case RecordInventory(id, value) =>
      log.info("Recorded inventory reading (request id {}) with {}", id, value)
      sender() ! InventoryRecorded(id)
      context.become(doReceive(availability + value))

    case ReadInventory(id) =>
      log.debug("ReadInventory requested, request id: {}", id)
      sender() ! RespondInventory(id, availability)
  }
}
