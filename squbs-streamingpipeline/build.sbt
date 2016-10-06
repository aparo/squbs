import Versions._

name := "squbs-streamingpipeline"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTest % "test->*",
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-agent" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "ch.qos.logback" % "logback-classic" % logbackClassic % "test"
)

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "report/squbs-streamingpipeline")

updateOptions := updateOptions.value.withCachedResolution(true)
