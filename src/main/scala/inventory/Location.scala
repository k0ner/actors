package inventory

import akka.actor.{Actor, ActorLogging, Props}
import inventory.queries.{InventoryRecorded, ReadInventory, RecordInventory, RespondInventory}

object Location {
  def props(item: ItemId, store: StoreId): Props = Props(new Location(item, store))
}

class Location(item: ItemId, store: StoreId) extends Actor with ActorLogging {

  var lastInventoryPicture = 0

  override def preStart(): Unit = log.info("Location actor {}-{} started", item, store)

  override def postStop(): Unit = log.info("Location actor {}-{} stopped", item, store)

  override def receive = {
    case RequestTrackLocation(`store`, `item`) =>
      log.info("Registering Location {}-{}", store, item)
      sender() ! LocationRegistered

    case RequestTrackLocation(requestedStore, requstedItem) =>
      log.warning("Ignoring TrackLocation request for {}-{}. This actor is responsible for {}-{}.",
        requestedStore, requstedItem, this.store, this.item)

    case RecordInventory(id, value) =>
      log.info("Recorded inventory reading {} with {}", id, value)
      lastInventoryPicture += value
      sender() ! InventoryRecorded(id)

    case ReadInventory(id) =>
      log.debug("ReadInventory requested, id: {}", id)
      sender() ! RespondInventory(id, lastInventoryPicture)
  }
}

case class ItemId(value: String)

case class StoreId(value: String)

case class RequestTrackLocation(store: StoreId, item: ItemId)

object LocationRegistered
