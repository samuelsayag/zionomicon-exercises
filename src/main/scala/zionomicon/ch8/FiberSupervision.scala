package zionomicon.ch8

import zio._
import zio.console._
import zio.duration._
import zio.{ App => ZIOApp }

object FiberSupervision {}

object FiberSuperLocalScope1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    neverEnding *> putStrLn("End of prog...").exitCode

  def neverEnding = for {
    _     <- putStrLn("enter the effect")
    fiber <- putStrLn("enter the never") *> ZIO.never.fork // ending immediatly if not "joined"
    // _ <- putStrLn("enter the never") *> ZIO.never // never ending
    _ <- fiber.join
  } yield ()

}

object FiberSuperLocalScope2 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = prog1.exitCode

  val childEffect =
    putStrLn("I am the child effect").delay(1.seconds)

  val parentEffect =
    putStrLn("I am the parent effect") *> childEffect.fork
  // Put the join to see the childEffect terminating
  // >>= (e => e.join)

  val prog1 = parentEffect.fork *> ZIO.never
}

object FiberSuperGlobalScope extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    // first part LocalScope/GlobalScope
    // module.exitCode
    program.tap(r => putStrLn(s"Result is [$r]")).exitCode

  val effect = for {
    //_ <- putStrLn("hearbeat !").delay(1.second).forever.fork
    // when reaching the end of the LocalScope the fiber does not end
    _ <- putStrLn("hearbeat !").delay(1.second).forever.forkDaemon
    _ <- putStrLn("Doing some effect work !")
    // to see the hearbeat, else the end of the effect scope kill the fiber
    // _ <- ZIO.sleep(5.seconds)
  } yield 42

  val module = for {
    fiber  <- effect.fork
    _      <- putStrLn("Doing a lot of big work...").delay(5.seconds)
    result <- fiber.join
  } yield result

  val program = for {
    // we would like the hearbeat to live during the life of the module ONLY!
    fiber <- module.fork
    _     <- putStrLn("Doing the main work of the application").delay(10.seconds)
    res   <- fiber.join
  } yield res

}

object FiberSuperZScope extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.tap(r => putStrLn(s"Program end with: [$r]")).exitCode

  def effect(scope: ZScope[Exit[Any, Any]]) = for {
    // scope the effect in `scope`
    _ <- putStrLn("hearbeat !").delay(1.second).forever.forkIn(scope)
    _ <- putStrLn("Done with the work of the effect...")
  } yield 42

  def module(scope: ZScope[Exit[Any, Any]]) = for {
    fiber  <- effect(scope).fork
    _      <- ZIO.sleep(5.seconds) *> putStrLn("Done with the work of the module...")
    result <- fiber.join
  } yield result

  val program = for {
    // create a scope for the hearbeat
    open  <- ZScope.make[Exit[Any, Any]]
    fiber <- module(open.scope).fork
    _     <- ZIO.sleep(10.seconds) *> putStrLn("Done with the work of the application...")
    res   <- fiber.join
    _     <- open.close(Exit.unit)
  } yield res

}
