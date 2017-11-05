package octostore.location

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import octostore.listing.RequestTrackListing

import scala.collection.mutable

object StoreManager {
  def props(): Props = Props(new StoreManager())
}

class StoreManager extends Actor with ActorLogging {

  val storeIdToActor = mutable.Map.empty[StoreId, ActorRef]
  val actorToStoreId = mutable.Map.empty[ActorRef, StoreId]

  override def preStart(): Unit = log.info("StoreManager started")

  override def postStop(): Unit = log.info("StoreManager  stopped")

  override def receive = {
    case trackMsg@RequestTrackListing(_, _, _) =>
      log.debug("RequestTrackListing {} for {}-{} received", trackMsg.requestId, trackMsg.store, trackMsg.listingId)
      storeIdToActor.get(trackMsg.store) match {
        case Some(storeActor) =>
          log.debug("Store actor found {}, forwarding message", storeActor)
          storeActor forward trackMsg
        case None =>
          log.info("Creating Store actor for {}", trackMsg.store)
          val storeActor = context.actorOf(Store.props(trackMsg.store), s"store-${trackMsg.store}")
          context.watch(storeActor)
          storeIdToActor.put(trackMsg.store, storeActor)
          actorToStoreId.put(storeActor, trackMsg.store)
          storeActor forward trackMsg
      }

    case RequestStoreList(requestId) =>
      log.debug("Store list requested {}", requestId)
      sender() ! ReplyStoreList(requestId, storeIdToActor.keySet)

    case Terminated(storeActor) =>
      val storeId = actorToStoreId(storeActor)
      log.info("Store actor for {} has been terminated", storeId)
      actorToStoreId.remove(storeActor)
      storeIdToActor.remove(storeId)
  }

}
