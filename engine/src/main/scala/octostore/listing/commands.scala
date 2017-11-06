package octostore.listing

import java.util.UUID

import octostore.location.LocationId

case class ReadInventory(requestId: UUID)

case class RespondInventory(requestId: UUID, value: Int)

case class RecordInventory(requestId: UUID, value: Int)

case class InventoryRecorded(requestId: UUID)

case class RequestTrackListing(requestId: UUID, locationId: LocationId, listingId: ListingId)

case class ListingRegistered(requestedId: UUID)
