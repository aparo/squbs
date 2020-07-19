import Versions._

name := "squbs-pattern"

testOptions in Test ++= Seq(
  Tests.Argument(TestFrameworks.ScalaTest, "-l", "org.squbs.testkit.tags.SlowTest"),
  Tests.Argument(TestFrameworks.JUnit, "-v", "-a")
)

javaOptions in Test ++= Seq("-Xmx512m", "-ea")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,
  "org.slf4j" % "slf4j-api" % slf4jV,
  "net.openhft" % "chronicle-queue" % chronicleQueueV % Optional,
  "org.scalatest" %% "scalatest" % scalatestV % Test,
  "junit" % "junit" % junitV % Test,
  "org.apache.commons" % "commons-math3" % "3.6.1" % Test,
  "com.novocode" % "junit-interface" % junitInterfaceV % Test,
  "com.wix" %% "accord-core" % accordV % Optional,
  "org.json4s" %% "json4s-jackson" % json4sV % Optional,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonV % Optional,
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % jacksonV % Optional,
  "ch.qos.logback" % "logback-classic" % logbackInTestV % Test
) ++ akkaDependencies

def akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-agent" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % Test,
  "com.typesafe.akka" %% "akka-contrib" % akkaV % Optional,
  "com.typesafe.akka" %% "akka-http" % akkaHttpV % Optional,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test
)

def sprayDependencies(v: String) = Seq(
  "io.spray" %% "spray-http" % v,
  "io.spray" %% "spray-httpx" % v,
  "io.spray" %% "spray-routing-shapeless2" % v,
  "io.spray" %% "spray-json" % "1.3.2" % "test",
  "io.spray" %% "spray-testkit" % v % "test",
  "io.spray" %% "spray-httpx" % v % "test"
)

// : Seq[sbt.Def.Setting[_]] in the line below is not required for a successful build
// however, it is added due to an intelliJ warning
Boilerplate.settings : Seq[sbt.Def.Setting[_]]

// (testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "report/squbs-pattern")

updateOptions := updateOptions.value.withCachedResolution(true)
