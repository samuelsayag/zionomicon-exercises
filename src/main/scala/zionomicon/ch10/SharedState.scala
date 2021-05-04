package zionomicon.ch10

import zio._
import zio.{App => ZIOApp}
import zio.console._
import java.util.concurrent.atomic.AtomicReference

object SharedState {

  // small non concurrent UNSAFE ref implementation as Var

  trait Var[A] {
    def get: UIO[A]
    def set(a: A): UIO[Unit]
    def modify(f: A => A): UIO[Unit]
  }

  object Var {

    def make[A](a: A): UIO[Var[A]] = UIO.effectTotal(new Var[A] {

      private var state: A = a

      def get: UIO[A] = UIO.effectTotal(state)

      def set(a: A): UIO[Unit] = UIO.effectTotal { state = a }

      def modify(f: A => A): UIO[Unit] = UIO.effectTotal { state = f(state) }
    })
  }

  // concurrent safe Ref
  trait Ref[A] {
    def modify[B](f: A => (B, A)): UIO[B]

    def get: UIO[A] = modify(a => (a, a))

    def set(a: A): UIO[Unit] = modify(_ => ((), a))

    def update(f: A => A): UIO[Unit] = modify(a => ((), f(a)))
  }

  object Ref {

    def make[A](a0: A): UIO[Ref[A]] = UIO.effectTotal {
      new Ref[A] {
        private val state                     = new AtomicReference[A](a0)
        def modify[B](f: A => (B, A)): UIO[B] = UIO.effectTotal {
          var loop = true
          var b    = null.asInstanceOf[B]
          while (loop) {
            val currentState = state.get()
            val res          = f(currentState)
            b = res._1
            loop = !state.compareAndSet(currentState, res._2)
          }
          b
        }
      }
    }
  }
}

object SharedStateUnsafe extends ZIOApp {

  import SharedState._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, Nothing, Unit] =
    for {
      ref   <- Var.make(0)
      _     <- ZIO.foreachPar_(1 to 10000)(_ => ref.modify(_ + 1))
      value <- ref.get
      _     <- putStrLn(s"The final value is: [$value]")
    } yield ()
}

object SharedStateSafe extends ZIOApp {

  import SharedState._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, Nothing, Unit] =
    for {
      ref   <- Ref.make(0)
      _     <- ZIO.foreachPar_(1 to 10000)(_ => ref.update(_ + 1))
      value <- ref.get
      _     <- putStrLn(s"The final value is: [$value]")
    } yield ()
}
