package zionomicon.ch10

import zio._
import zio.console._
import zio.{App => ZIOApp}

object FiberLocalState {

  // Per fiber capabilities logging structure
  // head contains the log for the current fiber
  // tail contains a list of Log for each of the fork fiber
  final case class Tree[+A](head: A, tail: List[Tree[A]])

  type Log = Tree[Chunk[String]]

  val makeLog: UIO[FiberRef[Log]] =
    FiberRef.make(
      Tree(Chunk.empty, List.empty),
      _ => Tree(Chunk.empty, List.empty),
      (parent, child) => parent.copy(tail = child :: parent.tail)
    )

  def log(ref: FiberRef[Log])(line: String): UIO[Unit] =
    ref.update(fsl => fsl.copy(head = fsl.head :+ line))
}

object FiberLocal1 extends ZIOApp {

  import FiberLocalState._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, Nothing, Unit] = for {
    ref    <- makeLog
    _      <- log(ref)("I am in the main fiber")
    fiber1 <- effect1(ref).fork
    fiber2 <- effect2(ref).fork
    _      <- fiber1.join
    _      <- fiber2.join
    log    <- ref.get
    _      <- putStrLn(log.toString)
  } yield ()

  def effect1(logRef: FiberRef[Log]): ZIO[Any, Nothing, Unit] =
    for {
      _ <- ZIO.effectTotal(1).tap(v => log(logRef)(s"I got value [$v]"))
      _ <- ZIO.effectTotal(3).tap(v => log(logRef)(s"I got value [$v]"))
    } yield ()

  def effect2(logRef: FiberRef[Log]): ZIO[Any, Nothing, Unit] =
    for {
      _ <- ZIO.effectTotal(2).tap(v => log(logRef)(s"I got value [$v]"))
      _ <- ZIO.effectTotal(4).tap(v => log(logRef)(s"I got value [$v]"))
    } yield ()
}
