name := "Clustered Chat"

scalacOptions ++= Seq("-deprecation")
scalaVersion := "2.11.7"
lazy val akkaV = "2.4.1"
lazy val akkaStreamsV = "2.0-M2"
libraryDependencies ++= Seq(
   "com.typesafe.akka" %% "akka-actor" % akkaV,
   "com.typesafe.akka" %% "akka-cluster" % akkaV,
   "com.typesafe.akka" %% "akka-cluster-tools" % akkaV,
   "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamsV,
   "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamsV
)
fork in run := true
connectInput in run := true