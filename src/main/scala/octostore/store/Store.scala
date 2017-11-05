package octostore.store

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import octostore.item.{Item, ItemId, RequestTrackLocation}

import scala.collection.mutable

case class StoreId(value: String) extends AnyVal {
  override def toString = value
}

object Store {
  def props(storeId: StoreId): Props = Props(new Store(storeId))
}

class Store(storeId: StoreId) extends Actor with ActorLogging {

  val itemIdToActor = mutable.Map.empty[ItemId, ActorRef]
  val actorToItemId = mutable.Map.empty[ActorRef, ItemId]

  override def preStart(): Unit = log.info("Store {} started", storeId)

  override def postStop(): Unit = log.info("Store {} stopped", storeId)

  override def receive = {
    case trackMsg@RequestTrackLocation(_, `storeId`, _) =>
      log.debug("RequestTrackLocation {} for {}-{} received", trackMsg.requestId, trackMsg.store, trackMsg.item)
      itemIdToActor.get(trackMsg.item) match {
        case Some(itemActor) =>
          log.debug("Item actor found {}, forwarding message", itemActor)
          itemActor forward trackMsg
        case None =>
          log.info("Creating Item actor for {}", trackMsg.item)
          val itemActor = context.actorOf(Item.props(trackMsg.item, trackMsg.store), s"item-${trackMsg.item}")
          context.watch(itemActor)
          itemIdToActor.put(trackMsg.item, itemActor)
          actorToItemId.put(itemActor, trackMsg.item)
          itemActor forward trackMsg
      }

    case RequestTrackLocation(id, requestedStoreId, _) =>
      log.warning("Ignoring TrackLocation request {} for {}. This actor is responsible for {}",
        id, requestedStoreId, storeId)

    case RequestItemList(requestId) =>
      log.debug("Item list requested {}", requestId)
      sender() ! ReplyItemList(requestId, itemIdToActor.keySet)

    case Terminated(itemActor) =>
      val itemId = actorToItemId(itemActor)
      log.info("Item actor for {} has been terminated", itemId)
      actorToItemId.remove(itemActor)
      itemIdToActor.remove(itemId)
  }
}
