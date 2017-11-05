package octostore.listing

import akka.actor.{Actor, ActorLogging, Props}
import octostore.location.LocationId

case class ListingId(value: String) extends AnyVal {
  override def toString = value
}

object Listing {
  def props(listingId: ListingId, locationId: LocationId): Props = Props(new Listing(listingId, locationId))
}

class Listing(listingId: ListingId, locationId: LocationId) extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("Listing actor {}-{} started", listingId, locationId)

  override def postStop(): Unit = log.info("Listing actor {}-{} stopped", listingId, locationId)

  override def receive = doReceive(0)

  def doReceive(availability: Int): Receive = {
    case RequestTrackListing(requestId, `locationId`, `listingId`) =>
      log.info("Registering listing {}-{} for requestId: {}", locationId, listingId, requestId)
      sender() ! ListingRegistered(requestId)

    case RequestTrackListing(id, requestedLocationId, requestedListingId) =>
      log.warning(s"Ignoring TrackListing request (request id $id) for $requestedLocationId-$requestedListingId." +
        s" This actor is responsible for ${this.locationId}-${this.listingId}.")

    case RecordInventory(id, value) =>
      log.info("Recorded inventory reading (request id {}) with {}", id, value)
      sender() ! InventoryRecorded(id)
      context.become(doReceive(availability + value))

    case ReadInventory(id) =>
      log.debug("ReadInventory requested, request id: {}", id)
      sender() ! RespondInventory(id, availability)
  }
}
