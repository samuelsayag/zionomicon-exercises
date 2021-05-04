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

object InterruptibleRegion1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.tap(res => putStrLn(s"We got [$res]")).exitCode

  def program = for {
    ref   <- Ref.make(false)
    fiber <- (ZIO.sleep(5.seconds) *> ref.set(true)).uninterruptible.fork
    //fiber <- (ZIO.sleep(5.seconds) *> ref.set(true)).uninterruptible.delay(10.millis).fork
    //fiber <- (ZIO.sleep(5.seconds) *> ref.set(true)).fork.uninterruptible
    //fiber <- (ZIO.sleep(5.seconds).interruptible *> ref.set(true)).fork.uninterruptible
    _     <- fiber.interrupt
    value <- ref.get
  } yield value
}

object InterruptibleRegion2 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val effect1 = putStrLn("end of effect 1").delay(1.second)
  val effect2 = putStrLn("end of effect 2").delay(2.second)
  val effect3 = putStrLn("end of effect 3").delay(3.second)

  val effectUninterruptible = (effect1 *> effect2 *> effect3).uninterruptible

  val effectInterruptibleRegion = (effect1 *> effect2 *> effect3.interruptible).uninterruptible

  val program = for {
    //fiber <- effectUninterruptible.fork
    fiber <- effectInterruptibleRegion.fork
    _     <- fiber.interrupt
  } yield ()

}

object WaitingForInterruption1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  def effect[E, A](promise: Promise[E, A], a: A) =
    putStrLn("Entering the effect") *> promise.succeed(a) *> ZIO.never

  val program = for {
    promise  <- Promise.make[Nothing, Unit]
    finalizer = putStrLn("Finalizing effect...").delay(3.seconds) *> putStr("end of finalizer...")
    // with this wait for the finalizer to finish which is normal
    fiber    <- effect(promise, ()).ensuring(finalizer).disconnect.fork
    //fiber    <- effect(promise, ()).ensuring(finalizer).fork
    _        <- promise.await
    _        <- fiber.interrupt
    //_        <- fiber.interrupt.fork
    _        <- putStrLn("Done interrupting...")
  } yield ()
}