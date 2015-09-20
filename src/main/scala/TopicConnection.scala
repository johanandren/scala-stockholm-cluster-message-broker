import java.util.UUID

import akka.actor._

object TopicConnection {

  /** message for incoming and outgoing messages to the client */
  case class Message(text: String)

  /** message to distribute across cluster */
  case class DistributedMessage(id: UUID, text: String)

  /** special message that will provide us with an outgoing sink to send messages to the client through */
  case class OutgoingDestination(destination: ActorRef)

  def props(topic: String) = Props(new TopicConnection(topic))
}

class TopicConnection(topic: String) extends Actor with ActorLogging {

  import TopicConnection._

  val id = UUID.randomUUID()
  var client = context.system.deadLetters
  log.info("Client session {} started", id.toString)

  // TODO subscribe to topic

  def receive = {
    case Message(text) =>
      log.info("Got message from client {}: {}", id, text)
      // TODO publish to topic

    case msg @ DistributedMessage(uuid, text) if uuid != id =>
      log.info("Got message from other cluster node: {}", msg)
      // TODO send to client


    case OutgoingDestination(destination) =>
      client = destination
      context.watch(destination)

    case Terminated(who) if who == client =>
      // if we cannot send messages, we might just as well shut down business
      context.stop(self)

  }


  override def postStop(): Unit = {
    log.info("Stopping client session {}", id.toString)
  }
}
