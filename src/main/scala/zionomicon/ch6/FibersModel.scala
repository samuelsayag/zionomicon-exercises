package zionomicon.ch6

import zio._
import zio.console._
import zio.clock._
import zio.duration._
import zio.{ App => ZIOApp }

object FibersModel {}

object FiberForkJoin extends ZIOApp {

  def run(args: List[String]) = program.exitCode

  def program: ZIO[ZEnv, Nothing, Unit] = for {
    f <- ZIO.sleep(5.seconds).fork
    _ <- (putStrLn("I am a little teapot-") *> putStrLn("I am a little teapot+")).repeatN(100000)
    _ <- f.join
  } yield ()
}

object FiberDefaultSupervision extends ZIOApp {

  def run(args: List[String]) = program.exitCode

  val child: ZIO[Clock with Console, Nothing, Unit] =
    putStrLn("Enter child fiber") *>
      ZIO.sleep(5.seconds) *> putStrLn("Hello from a child fiber")

  val parent: ZIO[Clock with Console, Nothing, Unit] =
    child.fork *> putStrLn("Enter parent fiber") *> ZIO.sleep(3.seconds) *> putStrLn(
      "Hello from a parent fiber"
    )

  val program: ZIO[ZEnv, Nothing, Unit] = for {
    fiber <- parent.fork
    _     <- ZIO.sleep(1.second)
    _     <- fiber.interrupt
    _     <- ZIO.sleep(10.seconds)
  } yield ()

}

object FiberForkDaemon extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv,ExitCode] = program *> putStrLn("Exiting...").exitCode

  val child: ZIO[Clock with Console, Nothing, Unit] =
    putStrLn("Enter child fiber (as daemon)") *>
      ZIO.sleep(13.seconds) *> putStrLn("Out of the child")

  val parent: ZIO[Clock with Console, Nothing, Unit] =
    child.forkDaemon *> putStrLn("Enter parent fiber") *> ZIO.sleep(3.seconds) *> putStrLn(
      "Hello from a parent fiber"
    )

  val program: ZIO[ZEnv, Nothing, Unit] = for {
    fiber <- parent.fork
    _     <- ZIO.sleep(1.second)
    _     <- fiber.interrupt
    _     <- ZIO.sleep(10.seconds)
  } yield ()

}

object FiberForkAwait extends ZIOApp {

  def run(args: List[String]) = program.exitCode

  val work: UIO[Int] = ZIO.effectTotal(throw new Exception("boom!"))

  val program: ZIO[ZEnv, Nothing, Unit] = for {
    fiber <- work.fork
    _     <- ZIO.sleep(5.second)
    i     <- fiber.await.map(_.getOrElse(_ => 555))
    _     <- putStrLn(s"The number computed is: [$i]")
  } yield ()

}

object FiberForkPoll extends ZIOApp {

  def run(args: List[String]) = program.exitCode

  val work: URIO[Clock, Int] = ZIO.sleep(10.seconds) *> ZIO.effectTotal(555)

  val program: ZIO[ZEnv, Nothing, Unit] = for {
    fiber <- work.fork
    i     <- fiber.await.map(_.getOrElse(_ => 555))
    _     <- putStrLn(s"The number computed is: [$i]")
  } yield ()

}
