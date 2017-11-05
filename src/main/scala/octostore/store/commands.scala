package octostore.store

import java.util.UUID

import octostore.listing.ListingId

case class RequestListings(requestId: UUID)

case class ReplyListings(requestId: UUID, listings: Iterable[ListingId])

case class RequestStoreList(requestId: UUID)

case class ReplyStoreList(requestId: UUID, storeList: Iterable[StoreId])
