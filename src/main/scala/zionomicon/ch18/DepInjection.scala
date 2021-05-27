package zionomicon.ch18

import zio._
import zio.console._
import zio.clock._

// Environment are present in ZIO via the Reader Mondad pattern
object DepInjection {

  trait Database

  trait Logging {
    def unsafeLog(line: String): Unit
    def log(line: String): UIO[Unit]
  }

  def getName(userId: Long): RIO[Database, String] = ???

  def audit(line: String): URIO[Logging, Unit] = ???

  // accessing the environment

  def logLine(line: String): ZIO[Logging, Nothing, Unit] =
    ZIO.environment[Logging].flatMap { logging =>
      //...doing some interesting stuff...
      logging.log(line)
    }

  def logLine1(line: String): ZIO[Logging, Nothing, Unit] =
    ZIO.access { logging =>
      logging.unsafeLog(line)
    }

  def logLine3(line: String): ZIO[Logging, Nothing, Unit] =
    ZIO.accessM { logging =>
      logging.log(line)
    }

  // composing the environment
  // it is obtain by the intersection type
  // 1 - order does not matter
  // 2 - introducing twice the same type does not change a thing
  // 3 - [-R] contravariant so `Any` is identity with respect to with

  type Env = Database with Logging

  object env extends Database with Logging {
    def log(line: String): UIO[Unit]  = ???
    def unsafeLog(line: String): Unit = ???
  }

  def getNameWithLog(userId: Long): RIO[Env, String] =
    for {
      name <- getName(userId)
      _    <- audit(s"Found the name in DB: $name")
    } yield name

  val req1: IO[Throwable, String] = getNameWithLog(12345678L).provide(env)

  // this is not practival, in practice ZLayer are used
  val req2: RIO[Logging, String] = getNameWithLog(12345678L).provideSome[Logging] { logging =>
    new Database with Logging {
      def log(line: String): UIO[Unit]  = logging.log(line)
      def unsafeLog(line: String): Unit = logging.unsafeLog(line)
    }
  }

  type Database2 = Has[Database2.Service]
  object Database2 {
    trait Service
  }
  type Logging2 = Has[Logging2.Service]
  object Logging2 {
    trait Service
  }
  lazy val databaseLayer: ZLayer[Any, Nothing, Database2]       =
    ???
  lazy val effect1: ZIO[Database2 with Logging2, Nothing, Unit] =
    ???
  lazy val effect2: ZIO[Logging2, Nothing, Unit]                =
    effect1.provideSomeLayer[Logging2](databaseLayer)

  // providing custom layers
  type Env2 = Database2 with Logging2 with Clock with Console

  lazy val effect3: ZIO[Env2, Nothing, Unit]                    =
    ???
  // provides all custom services
  lazy val layer: ZLayer[Any, Nothing, Database2 with Logging2] =
    ???
  // effect now only requires default services
  lazy val effect4: ZIO[ZEnv, Nothing, Unit]                    =
    effect1.provideCustomLayer(layer)
}
