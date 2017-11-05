package octostore.listing

import java.util.UUID

import octostore.store.StoreId

case class ReadInventory(requestId: UUID)

case class RespondInventory(requestId: UUID, value: Int)

case class RecordInventory(requestId: UUID, value: Int)

case class InventoryRecorded(requestId: UUID)

case class RequestTrackListing(requestId: UUID, store: StoreId, listingId: ListingId)

case class ListingRegistered(requestedId: UUID)
