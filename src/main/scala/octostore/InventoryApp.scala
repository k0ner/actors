package octostore

import akka.actor.ActorSystem

import scala.io.StdIn

object InventoryApp extends App {

  val system = ActorSystem("inventory-system")

  try {
    system.actorOf(InventorySupervisor.props(), "inventory-supervisor")
    // Create top level supervisor
    StdIn.readLine()
  } finally {
    system.terminate()
  }

}
