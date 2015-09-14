name := "Clustered Chat"

scalaVersion := "2.11.7"
lazy val akkaV = "2.4.0-RC2"
libraryDependencies ++= Seq(
   "com.typesafe.akka" %% "akka-actor" % akkaV,
   "com.typesafe.akka" %% "akka-cluster" % akkaV,
   "com.typesafe.akka" %% "akka-cluster-tools" % akkaV
)
fork in run := true
connectInput in run := true