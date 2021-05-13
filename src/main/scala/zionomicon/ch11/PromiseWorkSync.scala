package zionomicon.ch11

import zio._
import zio.console._
import zio.duration._
import zio.{App => ZIOApp}
import zio.clock.Clock

// simple example to expose the way synch work thanks to Promise
object PromiseSimple1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, Nothing, Unit] = for {
    promise <- Promise.make[Nothing, Unit]
    fiber1  <- (putStrLn("Hello, ") *> promise.succeed(())).fork
    fiber2  <- (promise.await *> putStrLn(" World!")).fork
    _       <- fiber1.join *> fiber2.join
  } yield ()
}

object PromiseCompleteEffect extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    //program1.exitCode
    program2.exitCode

  val genInt: UIO[Int] = ZIO.effectTotal(scala.util.Random.nextInt())

  def program1: ZIO[Console, Nothing, Unit] = for {
    promise <- Promise.make[Nothing, Int]
    _       <- promise.complete(genInt)
    t       <- (promise.await <*> promise.await)
    _       <- putStrLn(s"The result tuple is [$t]")
  } yield ()

  def program2: ZIO[Console, Nothing, Unit] = for {
    promise <- Promise.make[Nothing, Int]
    _       <- promise.completeWith(genInt)
    t       <- (promise.await <*> promise.await)
    _       <- putStrLn(s"The result tuple is [$t]")
  } yield ()
}

object PromiseFail extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.exitCode

  val program: ZIO[Console with Clock, Exception, String] =
    for {
      promise <- Promise.make[Exception, String]
      _       <- (putStrLn("Going to set promise").delay(3.seconds) *> promise.fail(
                   new Exception("Boom!")
                 )).fork
      fiber2  <- promise.await.fork
      value   <- fiber2.join
    } yield value
}

object PromiseDie extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.exitCode

  val program: ZIO[Console with Clock, Exception, String] =
    for {
      promise <- Promise.make[Exception, String]
      _       <- (putStrLn("Going to set promise").delay(3.seconds) *> promise.die(
                   new Exception("Boom!")
                 )).fork
      fiber2  <- promise.await.fork
      value   <- fiber2.join
    } yield value
}

object PromiseHalt extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.exitCode

  val program: ZIO[Console with Clock, Exception, String] =
    for {
      promise <- Promise.make[Exception, String]
      _       <- (putStrLn("Going to set promise").delay(3.seconds) *> promise.halt(
                   Cause.Fail(new Exception("Boom!"))
                 )).fork
      fiber2  <- promise.await.fork
      value   <- fiber2.join
    } yield value
}

object PromiseDone extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.exitCode

  val program: ZIO[Console with Clock, Exception, String] =
    for {
      promise <- Promise.make[Exception, String]
      _       <- (putStrLn("Going to set promise").delay(3.seconds) *> promise.done(
                   Exit.fail(new Exception("Boom!"))
                 )).fork
      fiber2  <- promise.await.fork
      value   <- fiber2.join
    } yield value
}

object PromiseInterrupt extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  def waitForPromWithDuration[E, A](p: Promise[E, A], n: Duration): ZIO[Console with Clock, E, A] =
    for {
      coeff <- ZIO.effectTotal(n * 100)
      _     <- putStrLn(s"Fiber with duration [$coeff]")
      value <- p.await.delay(5.seconds) <&> ZIO.sleep(coeff)
    } yield value._1

  val program: ZIO[Console with Clock, Exception, Unit] =
    for {
      promise <- Promise.make[Exception, Unit]
      fibers  <-
        ZIO.collectAllPar((1 to 3).map(i => waitForPromWithDuration(promise, i.seconds).fork))
      _       <- promise.interrupt.delay(10.seconds).fork
      _       <- Fiber.joinAll(fibers)
    } yield ()
}
