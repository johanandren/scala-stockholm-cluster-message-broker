import akka.actor.{Address, RootActorPath, ActorSystem}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object ClusterApp extends App {

  import Utils._

  val (ip, port) = args.toList match {
    case IpPort(ip, port) :: Nil => (ip, port.toInt)
    case _ =>
      throw new RuntimeException("Invalid startup parameters\nUsage: server [ip:port]")
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

  val system = ActorSystem("MessageBroker", config)

  // 1. startup cluster (already sort of done)

  // 2. setup distributed pub-sub

  // 3. profit


  println("Kill with 'q'")
  Stream.continually(StdIn.readLine()).takeWhile(_ != "q")

  // gracefully leave the cluster
  Cluster(system).leave(Cluster(system).selfAddress)
  waitUntil(Cluster(system).isTerminated)

  Await.result(system.terminate(), Duration.Inf)

}
