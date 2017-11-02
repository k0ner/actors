package inventory.item

import akka.actor.{Actor, ActorLogging, Props}
import inventory.store.StoreId

case class ItemId(value: String) extends AnyVal {
  override def toString = value
}

object Item {
  def props(item: ItemId, store: StoreId): Props = Props(new Item(item, store))
}

class Item(item: ItemId, store: StoreId) extends Actor with ActorLogging {

  var lastInventoryPicture = 0

  override def preStart(): Unit = log.info("Location actor {}-{} started", item, store)

  override def postStop(): Unit = log.info("Location actor {}-{} stopped", item, store)

  override def receive = {
    case RequestTrackLocation(id, `store`, `item`) =>
      log.info("Registering Item {}-{} for id: {}", store, item, id)
      sender() ! LocationRegistered(id)

    case RequestTrackLocation(id, requestedStore, requestedItem) =>
      log.warning(s"Ignoring TrackLocation request $id for $requestedStore-$requestedItem." +
        s" This actor is responsible for ${this.store}-${this.item}.")

    case RecordInventory(id, value) =>
      log.info("Recorded inventory reading {} with {}", id, value)
      lastInventoryPicture += value
      sender() ! InventoryRecorded(id)

    case ReadInventory(id) =>
      log.debug("ReadInventory requested, id: {}", id)
      sender() ! RespondInventory(id, lastInventoryPicture)
  }
}
