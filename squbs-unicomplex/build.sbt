import Versions._

name := "squbs-unicomplex"

javaOptions in Test += "-Xmx512m"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % scalaTest % "test->*",
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging,
  "ch.qos.logback" % "logback-classic" % logbackClassic % "test",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0",
  "com.wix" %% "accord-core" % "0.5"
) ++ akka(akkaV) ++ spray(sprayV)

def akka(v: String) = Seq(
  "com.typesafe.akka" %% "akka-actor" % v,
  "com.typesafe.akka" %% "akka-agent" % v,
  "com.typesafe.akka" %% "akka-http-experimental" % v,
  "com.typesafe.akka" %% "akka-testkit" % v % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % v % "test"
)

def spray(v: String) = Seq(
  "io.spray" %% "spray-can" % v,
  "io.spray" %% "spray-http" % v,
  "io.spray" %% "spray-routing-shapeless2" % v,
  "io.spray" %% "spray-testkit" % v % "test",
  "io.spray" %% "spray-client" % v % "test",
  "io.spray" %% "spray-json" % sprayJson % "test"
)

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "report/squbs-unicomplex")

updateOptions := updateOptions.value.withCachedResolution(true)
