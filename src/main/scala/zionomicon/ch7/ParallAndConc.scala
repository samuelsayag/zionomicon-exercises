package zionomicon.ch7

import zio._
import zio.console._
import zio.duration._
import zio.{App => ZIOApp}
import zio.clock.Clock

object ParallAndConc

object ZipParEx extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    // successAndFail(5.seconds, 3.seconds).exitCode // wil execute both of the effects
    successAndFail(5.seconds, 3.seconds).exitCode // wil execute just the one that fails

  def successAndFail(
      sleep1: Duration,
      sleep2: Duration
  ): RIO[Clock with Console, (Unit, Nothing)] = {
    // succeed effect
    val e1 = ZIO.sleep(sleep1) *> putStrLn("==== The exit the first effect ====")
    // fail effect
    val e2 = ZIO.sleep(sleep2) *> ZIO.effect(throw new Exception("Boom!"))

    // e1.zipPar(e2)
    e1 <&> e2
  }

}

object RacingEx extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    // successAndSuccess(5.seconds, 3.seconds).exitCode // wil execute just the one that fails
    successAndFailure(5.seconds, 2.seconds).exitCode

  def successAndSuccess(
      sleep1: Duration,
      sleep2: Duration
  ): ZIO[Clock with Console, Nothing, Unit] = {
    // succeed effect
    val e1 = ZIO.sleep(sleep1) *> putStrLn("==== Exit the first effect ====")
    val e2 = ZIO.sleep(sleep2) *> putStrLn("==== Exit the second effect ====")

    // e1.raceEither(e2)
    (e1 <|> e2) >>= {
      case Left(_)  => putStrLn("Succeed with first value").unit
      case Right(_) => putStrLn("Succeed with second value").unit
    }

  }

  def successAndFailure(
      sleep1: Duration,
      sleep2: Duration
  ): ZIO[Clock with Console, Throwable, Unit] = {
    // succeed effect
    val e1 = ZIO.sleep(sleep1) *> putStrLn("==== Exit the first effect ====")
    // fail effect
    val e2 = ZIO.sleep(sleep2) *> ZIO.effect(throw new Exception("Boom!"))

    // e1.raceEither(e2)
    (e1 race e2) *> putStrLn("End without error")
  }
}
