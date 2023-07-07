import com.scalapenos.sbt.prompt._

///////////////////////////////////////////////////////////////////////////////////////////////////
// Settings
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val commonSettings = Seq(
  organization := "me.milanvdm",
  scalaVersion := "2.13.11",
  resolvers ++= Seq(
    "confluent" at "https://packages.confluent.io/maven",
    "Kaluza artifactory" at "https://kaluza.jfrog.io/artifactory/maven"
  ),
  scalafmtOnCompile := true,
  incOptions := incOptions.value.withLogRecompileOnMacro(false),
  scalacOptions ++= commonScalacOptions,
  Test / fork := true,
  Test / parallelExecution := false,
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  promptTheme := PromptTheme(
    List(
      text("[SBT] ", fg(136)),
      currentProject(fg(64)).padRight(": ")
    )
  )
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Xlog-reflective-calls",
  "-Ydelambdafy:method",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard"
)

lazy val dockerSettings = Seq(
  name := "kafka-d3",
  dockerBaseImage := "openjdk:jre-alpine",
  Docker / packageName := name.value,
  Docker / version := version.value
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

///////////////////////////////////////////////////////////////////////////////////////////////////
// Dependencies
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val D = new {

  val Versions = new {
    val avro4s = "4.1.1"
    val cats = "2.9.0"
    val catsEffect = "3.5.1"
    val circe = "0.14.5"
    val fs2 = "3.7.0"
    val http4s = "0.23.22"
    val kafka = "3.5.0"
    val kafkaConfluent = "7.4.0"
    val pureConfig = "0.17.4"

    // Test
    val scalaTest = "3.2.16"
  }

  val avro4s = "com.sksamuel.avro4s" %% "avro4s-core" % Versions.avro4s
  val avroSerdes = "io.confluent" % "kafka-streams-avro-serde" % Versions.kafkaConfluent
  val cats = "org.typelevel" %% "cats-core" % Versions.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
  val circe = "io.circe" %% "circe-core" % Versions.circe
  val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe
  val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2
  val http4sServer = "org.http4s" %% "http4s-ember-server" % Versions.http4s
  val http4sCirce = "org.http4s" %% "http4s-circe" % Versions.http4s
  val http4sClient = "org.http4s" %% "http4s-ember-client" % Versions.http4s
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s
  val kafkaClient = "org.apache.kafka" % "kafka-clients" % Versions.kafka
  val kafkaSchemaRegistryClient = "io.confluent" % "kafka-schema-registry-client" % Versions.kafkaConfluent
  val kafkaStreams = "org.apache.kafka" %% "kafka-streams-scala" % Versions.kafka
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig

  // Test
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Projects
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val `kafka-d3` = Project(
  id = "kafka-d3",
  base = file(".")
).settings(moduleName := "kafka-d3")
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(core)
  .dependsOn(core)

lazy val core = Project(
  id = "core",
  base = file("core")
).settings(moduleName := "core")
  .settings(commonSettings)
  .settings(dockerSettings)
  .settings(Revolver.settings)
  .settings(
    libraryDependencies ++= Seq(
      D.avro4s,
      D.avroSerdes,
      D.cats,
      D.catsEffect,
      D.circe,
      D.circeGeneric,
      D.fs2,
      D.http4sServer,
      D.http4sCirce,
      D.http4sClient,
      D.http4sDsl,
      D.kafkaClient,
      D.kafkaStreams,
      D.kafkaSchemaRegistryClient,
      D.pureConfig,
      D.scalaTest % "it,test"
    )
  )
  .configs(IntegrationTest extend Test)
  .settings(Defaults.itSettings)
  .settings(
    IntegrationTest / fork := true,
    IntegrationTest / parallelExecution := false,
    inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings)
  )

///////////////////////////////////////////////////////////////////////////////////////////////////
// Plugins
///////////////////////////////////////////////////////////////////////////////////////////////////

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

///////////////////////////////////////////////////////////////////////////////////////////////////
// Commands
///////////////////////////////////////////////////////////////////////////////////////////////////

addCommandAlias("update", ";dependencyUpdates")
addCommandAlias("fcompile", ";scalafmtSbt;compile;test:compile;it:compile")
