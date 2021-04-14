package zionomicon.ch2

import zio._
import scala.io.Source
import zio.console._
import zio.random._
import java.io.IOException
import zio.{App => ZIOApp}

object Exercize {

  // ex1
  def readFileZio(file: String): Task[Iterable[String]] =
    ZIO.effect {
      val s   = Source.fromFile(file)
      val res = s.getLines().to(Iterable)
      s.close()
      res
    }

  // ex2
  def writeFileZio(file: String, text: Iterable[String]): Task[Unit] =
    ZIO {
      import java.io._
      val pw = new PrintWriter(new File(file))
      text.foreach(pw.write)
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
  val forExample2: ZIO[Random with Console, IOException, Unit] =
    for {
      _     <- putStrLn("Guess a number from 1 to 3:")
      guess <- getStrLn.map(_.toInt)
      i     <- nextIntBounded(3)
      ib = i + 1
      _ <-
        if (ib == guess) putStrLn("You guess right!")
        else putStrLn(s"You guess wrong, it was $ib")
    } yield ()

  // ex 11
  def eitherToZIO[E, A](either: Either[E, A]): IO[E, A] =
    either.fold(ZIO.fail(_), ZIO.succeed(_))

  // ex 12
  def listToZIO[A](list: List[A]): IO[None.type, A] =
    list.headOption.fold[IO[None.type, A]](ZIO.fail(None))(ZIO.succeed(_))

  // ex 13
  def currentTime(): UIO[Long] = ZIO.effectTotal(System.currentTimeMillis())

  // ex 14
  def getCacheValue(key: String, success: String => Unit, failure: Throwable => Unit) = ???

  def getCacheValueZIO(key: String): ZIO[Any, Throwable, String] = ZIO.effectAsync {
    (cbk: Task[String] => Unit) =>
      getCacheValue(key, s => cbk(ZIO.succeed(s)), th => cbk(ZIO.fail(th)))
  }

  // ex 15
  trait User
  def saveUserRecord(user: User, onSuccess: () => Unit, onFailure: Throwable => Unit): Unit = ???

  def saveUserRecordZIO(user: User): Task[Unit] =
    ZIO.effectAsync { (cbk: Task[Unit] => Unit) =>
      saveUserRecord(user, () => cbk(ZIO.unit), th => cbk(ZIO.fail(th)))
    }

  // ex 16
  import scala.concurrent.{ExecutionContext, Future}
  trait Query
  trait Result

  def doQuery(query: Query)(implicit ec: ExecutionContext): Future[Result] = ???

  def doQueryZio(query: Query): Task[Result] =
    ZIO.fromFuture((ec: ExecutionContext) => doQuery(query)(ec))

  // ex 19
  def readUntil(acceptInput: String => Boolean): ZIO[Console, IOException, String] =
    getStrLn >>= (in => ZIO.cond(acceptInput(in), in, ()) <> readUntil(acceptInput))

  // ex 20
  def doWhile[R, E, A](body: ZIO[R, E, A])(condition: A => Boolean): ZIO[R, E, A] =
    body >>= (res => ZIO.cond(condition(res), res, ()) <> doWhile(body)(condition))
}

// ex10
object Cat extends ZIOApp {
  import Exercize._

  def run(args: List[String]): URIO[Console with Console, ExitCode] = (for {
    path  <- ZIO.getOrFail(args.headOption)
    lines <- readFileZio(path)
    _     <- ZIO.foreach(lines)(l => putStrLn(l))
  } yield ()).exitCode
}

// ex 17

object HelloHuman extends ZIOApp {

  def run(args: List[String]): URIO[Console with Console, ExitCode] =
    (for {
      _    <- putStrLn("Hello dear human, what is your lovely name?")
      name <- getStrLn
      _    <- putStrLn(s"Would you like to grabe a byte with me, $name?")
    } yield ()).exitCode
}

// ex 18
object NumberGuessing extends ZIOApp {

  val a = 0
  val b = 11

  def run(args: List[String]): URIO[Random with Console with Console, ExitCode] = (for {
    i   <- inputUntilRight
    rnd <- nextIntBetween(a, b)
    _ <-
      if (rnd == i) putStrLn("You guessed it man!")
      else putStrLn(s"Not even close dummy! [mine: $rnd] ")
  } yield ()).exitCode

  def inputUntilRight: URIO[Console, Int] =
    readInt <> (putStrLn(s"- Still not between [$a, $b]") *> inputUntilRight)

  def readInt: ZIO[Console, Any, Int] = for {
    _        <- putStrLn(s"- Guess wich Int, between $a and $b (excluded), I am thinking about?")
    input    <- getStrLn
    inputInt <- ZIO.effect(input.toInt)
    res      <- testIntBetween(a, b)(inputInt)
  } yield res

  def testIntBetween(a: Int, b: Int): Int => IO[Unit, Int] = (i: Int) =>
    ZIO.cond(
      i >= a && i < b,
      i,
      ()
    )
}
