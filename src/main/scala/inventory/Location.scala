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
    case ReadInventory(id) =>
      sender() ! RespondInventory(id, lastInventoryPicture)

    case RecordInventory(uuid, value) =>
      log.info("Recorded inventory reading {} with {}", uuid, value)
      lastInventoryPicture += value
      sender() ! InventoryRecorded(uuid)
  }
}

case class ItemId(value: String)

case class StoreId(value: String)