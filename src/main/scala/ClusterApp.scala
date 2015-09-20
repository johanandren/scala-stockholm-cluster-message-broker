import akka.actor.{Props, Address, RootActorPath, ActorSystem}
import akka.cluster.Cluster
import akka.cluster.client.ClusterClientReceptionist
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object ClusterApp extends App {

  import Utils._

  val (ip, port) = args.toList match {
    case IpPort(ip, port) :: Nil => (ip, port.toInt)
    case _ =>
      throw new RuntimeException("Invalid startup parameters\nUsage: server [ip:port] (port must be 2551 and upwards)")
  }

  val config = ConfigFactory.parseString(
    s"""
      | akka {
      |   remote {
      |     netty.tcp {
      |       hostname = "$ip"
      |       port = $port
      |     }
      |   }
      | }
    """.stripMargin).withFallback(ConfigFactory.load("cluster"))

  val system = ActorSystem("ChatCluster", config)

  val webPort = 9000 + port - 2551
  val webserver = system.actorOf(WebServer.props(ip, webPort), "webserver")

  println("Node started. Kill with 'q' + enter")
  Stream.continually(StdIn.readLine()).takeWhile(_ != "q")

  // gracefully leave the cluster
  Cluster(system).leave(Cluster(system).selfAddress)
  waitUntil(Cluster(system).isTerminated)

  Await.result(system.terminate(), Duration.Inf)

}
