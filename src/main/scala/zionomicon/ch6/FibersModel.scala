package zionomicon.ch6

import zio._
import zio.console._
import zio.clock._
import zio.duration._
import zio.{ App => ZIOApp }

object FibersModel {}

object FiberExample1 extends ZIOApp {

  def run(args: List[String]) = program.exitCode

  def program: ZIO[ZEnv, Nothing, Unit] = for {
    f <- ZIO.sleep(5.seconds).fork
    _ <- (putStrLn("I am a little teapot-") *> putStrLn("I am a little teapot+")).repeatN(100000)
    _ <- f.join
  } yield ()
}

object FiberExample2 extends ZIOApp {

  def run(args: List[String]) = program.exitCode

  val child: ZIO[Clock with Console, Nothing, Unit] =
    ZIO.sleep(5.seconds) *> putStrLn("Hello from a child fiber")

  val parent: ZIO[Clock with Console, Nothing, Unit] =
    child *> ZIO.sleep(3.seconds) *> putStrLn("Hello from a parent fiber")

  val program: ZIO[ZEnv, Nothing, Unit] = for {
    fiber <- parent.fork
    _     <- ZIO.sleep(1.second)
    _     <- fiber.interrupt
    _     <- ZIO.sleep(10.seconds)
  } yield ()

}
