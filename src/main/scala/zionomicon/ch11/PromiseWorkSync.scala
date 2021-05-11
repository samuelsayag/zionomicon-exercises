package zionomicon.ch11

import zio._
import zio.console._
import zio.{App => ZIOApp}

// simple example to expose the way synch work thanks to Promise
object PromiseSimple1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program = for {
    promise <- Promise.make[Nothing, Unit]
    fiber1  <- (putStrLn("Hello, ") *> promise.succeed(())).fork
    fiber2  <- (promise.await *> putStrLn(" World!")).fork
    _       <- fiber1.join *> fiber2.join
  } yield ()
}

// complete the promise when: defect, exiting, failing interrupting
// .die(t: Throwable)
// .done(e: Exit[E, A])
// .fail(e: E)
// .halt(e: Cause[E])

object PromiseCompleteEffect extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    //program1.exitCode
    program2.exitCode

  val genInt = ZIO.effectTotal(scala.util.Random.nextInt())

  def program1 = for {
    promise <- Promise.make[Nothing, Int]
    _       <- promise.complete(genInt)
    t       <- (promise.await <*> promise.await)
    _       <- putStrLn(s"The result tuple is [$t]")
  } yield ()

  def program2 = for {
    promise <- Promise.make[Nothing, Int]
    _       <- promise.completeWith(genInt)
    t       <- (promise.await <*> promise.await)
    _       <- putStrLn(s"The result tuple is [$t]")
  } yield ()
}
