package octostore.store

import java.util.UUID

import octostore.item.ItemId

case class RequestItemList(requestId: UUID)

case class ReplyItemList(requestId: UUID, itemList: Iterable[ItemId])

case class RequestStoreList(requestId: UUID)

case class ReplyStoreList(requestId: UUID, storeList: Iterable[StoreId])
