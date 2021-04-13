package zionomicon.ch2

import zio._
import scala.io.Source
import zio.console._
import zio.random._
import java.io.IOException

object Exercize {

  // ex1
  def readFileZio(file: String): Task[String] =
    ZIO.effect {
      val s   = Source.fromFile(file)
      val res = s.getLines().mkString
      s.close()
      res
    }

  // ex2
  def writeFileZio(file: String, text: String): Task[Unit] =
    ZIO {
      import java.io._
      val pw = new PrintWriter(new File(file))
      pw.write(text)
      pw.close()
    }

  // ex3
  def copyFileZio(source: String, dest: String): Task[Unit] =
    //readFileZio(source).flatMap(fc => writeFileZio(dest, fc))
    readFileZio(source) >>= (writeFileZio(dest, _))

  // ex4
  val forExample: ZIO[Console, IOException, Unit] = for {
    _    <- putStrLn("What is your name?")
    name <- getStrLn
    _    <- putStrLn(s"Hello, $name")
  } yield ()

  // ex5
  val forExample2: ZIO[Random with Console,IOException,Unit] =
    for {
      _     <- putStrLn("Guess a number from 1 to 3:")
      guess <- getStrLn.map(_.toInt)
      i     <- nextIntBounded(3)
      ib = i + 1
      _ <-
        if (ib == guess) putStrLn("You guess right!")
        else putStrLn(s"You guess wrong, it was $ib")
    } yield ()

}
