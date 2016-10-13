val akkaVersion = "2.4.11"
val squbsVersion = "0.8.0"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.squbs" %% "squbs-unicomplex" % squbsVersion,
  "org.scalatest" %% "scalatest" % scalaTest % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.squbs" %% "squbs-testkit" % squbsVersion % "test"
)
