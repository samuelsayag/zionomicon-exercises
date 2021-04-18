val zioVersion = "1.0.6"

inThisBuild(
  List(
    scalaVersion := "2.13.5",
    scalacOptions ++= Seq("-Wunused", "-deprecation"),
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
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
    )
  )

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

// warn-silent launch console
Compile / console / scalacOptions --= Seq("-Wunused")

console / initialCommands := """
  |println("ZIO main import")
  |import zio._
  |import zio.Runtime.{default => rt}
  |import zio.console._
  """.stripMargin

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
