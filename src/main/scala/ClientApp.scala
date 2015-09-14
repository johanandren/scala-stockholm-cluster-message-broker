import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object ClientApp extends App {

  val IpPort = """(\d{1,3}(?:\.\d{1,3}){3}):(\d+)""".r

  val (ip, port, topic) = args.toList match {
    case IpPort(ip, port) :: topic :: Nil => (ip, port.toInt, topic)
    case _ =>
      throw new RuntimeException("Subscribe and send messages for a given topic\nUsage: client [server-ip:server-port] [topic]")
  }

  println(s"Connecting to $ip:$port")

  val config = ConfigFactory.parseString(
    s"""
      |akka {
      |  cluster.client {
      |    initial-contacts = ["akka.tcp://MessageBroker@$ip:$port/system/receptionist"]
      |  }
      |}
    """.stripMargin).withFallback(ConfigFactory.load("client"))

  val system = ActorSystem("Client", config)

  // ... ??? ...

  // 1. connect to cluster - cluster client (a bit prepared already)

  // 2. register for events on topic, and print those

  // 3. send input to topic

  // 4. profit (?)

  val toCluster = system.deadLetters

  println("Write 'q' to quit and anything else to send message")
  Stream.continually(StdIn.readLine())
    .takeWhile(_ != "q")
    .foreach(message =>
      // this will need some changes later
      toCluster ! message
    )

  Await.result(system.terminate(), Duration.Inf)

}
