import Versions._

name := "squbs-actormonitor"

javaOptions in Test += "-Xmx512m"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "2.2.1" % "test->*",
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-agent" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging,
  "ch.qos.logback" % "logback-classic" % logbackClassic % "test"
)

updateOptions := updateOptions.value.withCachedResolution(true)

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "report/unicomplex")
