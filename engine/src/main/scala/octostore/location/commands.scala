package octostore.location

import java.util.UUID

import octostore.listing.ListingId

case class RequestListings(requestId: UUID)

case class ReplyListings(requestId: UUID, listings: Iterable[ListingId])

case class RequestLocations(requestId: UUID)

case class ReplyLocations(requestId: UUID, locations: Iterable[LocationId])
