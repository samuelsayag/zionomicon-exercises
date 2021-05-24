package zionomicon.ch15

import zio._
import zio.console._
import zio.{App => ZIOApp}
import scala.io.Source
import java.io.File

object BracketExample1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    showFile(args(0)).exitCode

  def showFile(path: String): ZIO[Console, Throwable, Unit] = for {
    lines <- fileAsList(path)
    _     <- ZIO.collectAll(lines.map(line => putStrLn(line)))
  } yield ()

  def fileAsList(path: String): Task[List[String]] =
    withFile(path)(s => s.getLines().toList)

  // Here it is just a sandbox example as the Source obect in Scala
  // is alredy build in order to release resource after usage
  def withFile[A](path: String)(use: Source => A): Task[A] = {
    // release() will run after use()
    // if interrupted after acuisition guarantee that release() will run
    val acquire: Task[Source] = IO(Source.fromFile(new File(path)))
    val release               = (r: Source) => URIO(r.close())
    val useZ                  = (s: Source) => Task(use(s))

    ZIO.bracket(acquire, release, useZ)
  }
}
