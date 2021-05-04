package zionomicon.ch4

import zio._
import zio.console._
import zio.{App => ZIOApp}

object ErrorModel

object DivByZero extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = (for {
    i <- arg(args)
    //r <- divByZero(i)
    // r <- divByZeroChecked(i)
    r <- divByZeroSurfaceCause(i)
    _ <- putStrLn(s"Result is: [$r]")
  } yield r).exitCode

  def arg(s: List[String]): Task[Int] = ZIO.effect(s(0).toInt)

  def divByZero(a: Int): UIO[Int] = ZIO.effectTotal(a / 0)

  def divByZeroChecked(a: Int): IO[ArithmeticException, Int] =
    ZIO.effect(a / 0).mapError { case e: ArithmeticException => e }

  def divByZeroSurfaceCause(a: Int): URIO[Console, Int]      =
    ZIO.effectTotal(a / 0).sandbox.tapError(e => putStrLn(e.prettyPrint)) <>
      ZIO.effectTotal(0)

}
