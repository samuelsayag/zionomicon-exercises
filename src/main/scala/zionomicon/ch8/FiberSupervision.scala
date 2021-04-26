package zionomicon.ch8

import zio._
import zio.console._
import zio.duration._
import zio.{ App => ZIOApp }

object FiberSupervision {}

object FiberSuper1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    neverEnding *> putStrLn("End of prog...").exitCode

  def neverEnding = for {
    _     <- putStrLn("enter the effect")
    fiber <- putStrLn("enter the never") *> ZIO.never.fork // ending immediatly if not "joined"
    // _ <- putStrLn("enter the never") *> ZIO.never // never ending
    _ <- fiber.join
  } yield ()

}

object FiberSuper2 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = prog1.exitCode

  def childEffect =
    putStrLn("I am the child effect").delay(1.seconds)

  def parentEffect =
    putStrLn("I am the parent effect") *> childEffect.fork
  // Put the join to see the childEffect terminating
  // >>= (e => e.join)

  def prog1 = parentEffect.fork *> ZIO.never
}
