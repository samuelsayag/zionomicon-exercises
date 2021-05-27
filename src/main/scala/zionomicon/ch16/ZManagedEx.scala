package zionomicon.ch16

import zio._
import zio.{ App => ZIOApp }
import java.io.IOException
import java.io.File
import scala.io.Source

object ZManagedExample1 extends ZIOApp {

  // composition of brackets

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  def openFile(path: String): IO[IOException, File] = ???

  def closeFile(file: File): UIO[Unit] = ???

  def withFile[A](path: String)(use: File => Task[A]): ZIO[Any with Any, Throwable, A] =
    openFile(path).bracket(closeFile)(use)

  def analyze(weatherData: File, results: File): Task[Unit] =
    ???

  lazy val program: ZIO[Any with Any, Throwable, Unit] =
    withFile("weather-data.txt") { weatherData =>
      withFile("results.txt")(results => analyze(weatherData, results))
    }
}

object ZManagedExample2 extends ZIOApp {

  // composition of ZManaged

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  def managedFile(path: String): IO[IOException, File] =
    ???

  def analyze(weatherData: File, results: File): Task[Unit] =
    ???

  lazy val dataFiles: ZIO[Any, IOException, (File, File)] =
    managedFile("weather-data.txt").zipPar(managedFile("results.txt"))

  lazy val program: ZIO[Any, IOException, Task[Unit]] = dataFiles.map { case (weather, result) =>
    analyze(weather, result)
  }
}

object ZManagedExample3 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = ???

  def file(path: String): ZManaged[Any, IOException, File] = ???

  lazy val names: List[String] = ???

  lazy val files: ZManaged[Any, Throwable, List[File]] =
    // ZManaged.foreachParN(4)(names)(file)
    ZManaged.foreach(names)(file)

  lazy val weatherData: Task[List[String]] =
    files.use { files =>
      Task
        .foreach(files) { file =>
          Task(Source.fromFile(file).getLines().toList)
        }
        .map(_.flatten)
    }

}
