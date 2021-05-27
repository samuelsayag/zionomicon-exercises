package zionomicon.ch3

import zio._
import zio.test._
import zio.duration._
import zio.console._
import zio.test.Assertion._
import zio.test.environment._
import java.io.IOException
import zio.clock.Clock

object TestServiceSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] = suite("myEnvTests")(consoleEnvTest, clockConsoleTest)

  /**
   * ZIO program involving just the console
   */
  val greet: ZIO[Console, IOException, Unit] =
    for {
      name <- getStrLn.orDie
      _    <- putStrLn(s"Hello, $name")
    } yield ()

  val consoleEnvTest: ZSpec[Console with TestConsole, IOException] =
    testM("Test the console env") {
      for {
        _     <- TestConsole.feedLines("Jane!")
        _     <- greet
        value <- TestConsole.output
      } yield assert(value)(equalTo(Vector(s"Hello, Jane!\n")))
    }

  val goShopping: ZIO[Console with Clock, IOException, Unit] =
    putStrLn("I am going shopping now...").delay(1.hour)

  val clockConsoleTest: ZSpec[Console with Clock with TestClock with TestConsole, IOException] =
    testM("Test console+clock env") {
      for {
        fiber  <- goShopping.fork
        _      <- TestClock.adjust(1.hour)
        _      <- fiber.join
        output <- TestConsole.output
        //} yield assertCompletes
      } yield assert(output)(equalTo(Vector("I am going shopping now...\n")))
    }
}
