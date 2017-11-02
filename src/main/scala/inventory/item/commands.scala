package inventory.item

import java.util.UUID

case class ReadInventory(requestId: UUID)

case class RespondInventory(requestId: UUID, value: Int)

case class RecordInventory(requestId: UUID, value: Int)

case class InventoryRecorded(requestId: UUID)

case class RequestTrackLocation(requestId: UUID, store: StoreId, item: ItemId)

case class LocationRegistered(requestedId: UUID)
