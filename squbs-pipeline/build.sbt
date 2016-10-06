import Versions._

name := "squbs-pipeline"

javaOptions in Test += "-Xmx512m"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % scalaTest % "test->*",
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-agent" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging,
  "io.spray" %% "spray-can"     % sprayV,
  "io.spray" %% "spray-http"    % sprayV,
  "io.spray" %% "spray-routing-shapeless2" % sprayV,
  "io.spray" %% "spray-testkit" % sprayV % "test",
  "io.spray" %% "spray-client"  % sprayV % "test",
  "io.spray" %% "spray-json"    % sprayJson % "test",
  "ch.qos.logback" % "logback-classic" % logbackClassic % "test"
)

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "report/squbs-pipeline")

updateOptions := updateOptions.value.withCachedResolution(true)
