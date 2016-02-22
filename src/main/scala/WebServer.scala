import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import akka.stream.scaladsl._
import akka.stream.{FlowShape, OverflowStrategy, ActorMaterializer}

object WebServer {
  def props(host: String, port: Int) = Props(new WebServer(host, port))
}

/**
 * Publishes http endpoints "/" and "/[topic]" and websocket endpoint "/[topic]/ws"
 */
class WebServer(host: String, port: Int) extends Actor with ActorLogging {

  case object Started

  import context.dispatcher
  implicit val materializer = ActorMaterializer()

  val routes =
    pathSingleSlash {
      getFromResource("index.html")
    } ~
    path(Segment / "ws") { topic =>
      get {
        handleWebsocketMessages(websocketFlow(topic, bufferSize = 10))
      }
    } ~
    path(Segment) { topic =>
      getFromResource("topic.html")
    }


  val bindingFuture = Http(context.system).bindAndHandle(routes, host, port)
  bindingFuture.foreach(_ => self ! Started)

  def receive = {
    case Started =>
      log.info("Web server started at http://{}:{}/", host, port)
  }

  override def postStop(): Unit = {
    log.info("Web at {}:{} stopping", host, port)
    bindingFuture.foreach(_.unbind())
  }


  /**
   * Creates a flow to use for the websocket, hiding the ws message model from the client actor
   *
   * Pretty hairy, might either show my lack of knowledge about streams or that there is some stuff still missing
   * in the akka-streams API:s
   *
   * Based on
   * https://github.com/oomagnitude/dashy/blob/master/jvm/src/main/scala/com/oomagnitude/dash/server/streams/Flows.scala#L72
   */
  def websocketFlow(topic: String, bufferSize: Int, overflowStrategy: OverflowStrategy = OverflowStrategy.fail): Flow[Message, Message, Any] = {

    val topicConnection = context.system.actorOf(TopicConnection.props(topic))

    // will materialize a destination actor, when materialized its actorRef is sent to the topic connection actor
    // so that it can send messages to it to pass them out to the websocket client
    val source: Source[Message, ActorRef] = Source.actorRef[TopicConnection.Message](bufferSize, OverflowStrategy.fail)
      .map(msg => TextMessage(msg.text))
      .mapMaterializedValue { destinationRef =>
        topicConnection ! TopicConnection.OutgoingDestination(destinationRef)
        destinationRef
      }

    // will pass messages incomming messages to the topic actor
    val sink =
      Flow[AnyRef]
        .map {
          case TextMessage.Strict(text) => TopicConnection.Message(text)
          case x => throw new RuntimeException("Unknown incoming message type")
        }
        .to(Sink.actorRef[AnyRef](topicConnection, PoisonPill))

    Flow.fromSinkAndSource(sink, source)
  }

}
