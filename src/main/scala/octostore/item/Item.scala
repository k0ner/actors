package octostore.item

import akka.actor.{Actor, ActorLogging, Props}
import octostore.store.StoreId

case class ItemId(value: String) extends AnyVal {
  override def toString = value
}

object Item {
  def props(item: ItemId, store: StoreId): Props = Props(new Item(item, store))
}

class Item(item: ItemId, store: StoreId) extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("Location actor {}-{} started", item, store)

  override def postStop(): Unit = log.info("Location actor {}-{} stopped", item, store)

  override def receive = doReceive(0)

  def doReceive(availability: Int): Receive = {
    case RequestTrackLocation(id, `store`, `item`) =>
      log.info("Registering Item {}-{} for id: {}", store, item, id)
      sender() ! LocationRegistered(id)

    case RequestTrackLocation(id, requestedStore, requestedItem) =>
      log.warning(s"Ignoring TrackLocation request $id for $requestedStore-$requestedItem." +
        s" This actor is responsible for ${this.store}-${this.item}.")

    case RecordInventory(id, value) =>
      log.info("Recorded inventory reading {} with {}", id, value)
      sender() ! InventoryRecorded(id)
      context.become(doReceive(availability + value))

    case ReadInventory(id) =>
      log.debug("ReadInventory requested, id: {}", id)
      sender() ! RespondInventory(id, availability)
  }
}
