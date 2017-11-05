package octostore.location

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import octostore.listing.RequestTrackListing

import scala.collection.mutable

object LocationManager {
  def props(): Props = Props(new LocationManager())
}

class LocationManager extends Actor with ActorLogging {

  val locationIdToActor = mutable.Map.empty[LocationId, ActorRef]
  val actorToLocationId = mutable.Map.empty[ActorRef, LocationId]

  override def preStart(): Unit = log.info("LocationManager started")

  override def postStop(): Unit = log.info("LocationManager  stopped")

  override def receive = {
    case trackMsg@RequestTrackListing(_, _, _) =>
      log.debug("RequestTrackListing {} for {}-{} received", trackMsg.requestId, trackMsg.locationId, trackMsg.listingId)
      locationIdToActor.get(trackMsg.locationId) match {
        case Some(locationActor) =>
          log.debug("Location actor found {}, forwarding message", locationActor)
          locationActor forward trackMsg
        case None =>
          log.info("Creating Location actor for {}", trackMsg.locationId)
          val locationActor = context.actorOf(Location.props(trackMsg.locationId), s"location-${trackMsg.locationId}")
          context.watch(locationActor)
          locationIdToActor.put(trackMsg.locationId, locationActor)
          actorToLocationId.put(locationActor, trackMsg.locationId)
          locationActor forward trackMsg
      }

    case RequestLocations(requestId) =>
      log.debug("Location list requested {}", requestId)
      sender() ! ReplyLocations(requestId, locationIdToActor.keySet)

    case Terminated(locationActor) =>
      val locationId = actorToLocationId(locationActor)
      log.info("Location actor for {} has been terminated", locationId)
      actorToLocationId.remove(locationActor)
      locationIdToActor.remove(locationId)
  }

}
