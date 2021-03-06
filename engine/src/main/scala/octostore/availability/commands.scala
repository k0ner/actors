package octostore.availability

import java.util.UUID

case class RequestAllAvailabilities(requestId: UUID)

case class RespondAllAvailabilities(requestId: UUID, availabilities: Map[String, AvailabilityReading])

sealed trait AvailabilityReading

case class Availability(quantity: Int) extends AvailabilityReading

case object NoAvailability extends AvailabilityReading

case object ListingNotReachable extends AvailabilityReading

case object ListingTimedOut extends AvailabilityReading
