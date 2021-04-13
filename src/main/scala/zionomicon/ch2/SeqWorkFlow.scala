package zionomicon.ch2

import zio._
import scala.io.StdIn

object SeqWorkFlow extends App {

  def zReadLine(prompt: String): Task[String] =
    ZIO.effect(StdIn.readLine(prompt))

  def printLine(line: String): Task[Unit] =
    ZIO.effect(println(line))

  val echo: ZIO[Any,Throwable,Unit] = for {
    line <- zReadLine("first name\n").zipWith(zReadLine("second name\n"))((f, s) =>
      s"name is $f $s"
    )
    _ <- printLine(line)
  } yield ()

  def run(args: List[String]) = echo.exitCode

}
