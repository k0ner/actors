package octostore

import akka.actor.ActorSystem

import scala.io.StdIn
import scala.util.Try

object InventoryApp extends App {

  val system = ActorSystem("inventory-system")

  Try(startInventorySystem)
  system.terminate()

  def startInventorySystem = {
    // Create top level supervisor
    system.actorOf(InventorySupervisor.props(), "inventory-supervisor")
    StdIn.readLine()
  }

}
