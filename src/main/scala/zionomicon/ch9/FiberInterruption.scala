package zionomicon.ch9

import zio._
import zio.console._
import zio.duration._
import zio.blocking
import zio.{App => ZIOApp}
import java.util.concurrent.atomic.AtomicBoolean

/** Interruption of Fiber
  *
  * - fiber.interrupt()
  */

// fiber that may never be executed in anyway
object InterruptSimple extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program = for {
    fiber <- putStrLn("Hello I am being executing in a new fiber").fork
    _     <- fiber.interrupt
  } yield ()
}

// program that interrupt also it is useless
object InterruptSimpleFlawed extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.tap(r => putStrLn(s"my result [$r]")).exitCode

  // not sure the fiber will be executed
  val program = for {
    ref   <- Ref.make(false)
    fiber <- ZIO.never.ensuring(ref.set(true)).fork
    _     <- fiber.interrupt
    value <- ref.get
  } yield value

  // the fiber will be executed with certainty
  val program2 = for {
    promise <- Promise.make[Nothing, Unit]
    ref     <- Ref.make(false)
    fiber   <- (promise.succeed(()) *> ZIO.never).ensuring(ref.set(true)).fork
// this await the result of the promise as garantee that it will be executed
    _       <- promise.await
    _       <- fiber.interrupt
    value   <- ref.get
  } yield value
}

object BlockingInterrupt1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program = for {
    ref   <- ZIO.succeed(new AtomicBoolean(false))
    fiber <- (effect(ref)).fork
    _     <- ZIO.sleep(10.millis)
    _     <- fiber.interrupt
  } yield ()

  def effect(cancel: AtomicBoolean) =
    blocking.effectBlockingCancelable {
      var i = 0
      while (i < 1000000 && !cancel.get) {
        println(i)
        i = i + 1
      }
    }(ZIO.effectTotal(cancel.set(true)))

}

object BlockingInterrupt2 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program = for {
    fiber <- effect.fork
    _     <- ZIO.sleep(10.millis)
    _     <- fiber.interrupt
    _     <- putStrLn("End of the program")
  } yield ()

  lazy val effect =
    blocking.effectBlockingInterrupt {
      Thread.sleep(10000L)
    }

}
