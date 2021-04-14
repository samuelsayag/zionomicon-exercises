import Dependencies._

inThisBuild(
  List(
    scalaVersion := "2.13.5",
    scalacOptions += "-Wunused",
    scalacOptions += "-deprecation",
    version := "0.1.0-SNAPSHOT",
    organization := "com.example",
    organizationName := "example",
    semanticdbEnabled := true,                        // scalafix: enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "learn-zio",
    libraryDependencies += scalaTest  % Test,
    libraryDependencies += "dev.zio" %% "zio" % "1.0.5"
  )

Compile / console / scalacOptions --= Seq("-Wunused")

console / initialCommands := """
  |println("ZIO main import")
  |import zio._
  |import zio.Runtime.{default => rt}
  |import zio.console._
  """.stripMargin

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
