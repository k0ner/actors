package octostore

import akka.actor.{Actor, ActorLogging, Props}

object InventorySupervisor {
  def props(): Props = Props(new InventorySupervisor)
}

class InventorySupervisor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("Inventory Application started")

  override def postStop(): Unit = log.info("Inventory Application stopped")

  override def receive = Actor.emptyBehavior
}
