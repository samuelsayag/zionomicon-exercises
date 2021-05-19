package zionomicon.ch10

import zio._
import zio.{App => ZIOApp}
import zio.console._
import java.util.concurrent.atomic.AtomicReference
import java.io.IOException

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

    def updateAndLog[A](r: Ref[A])(f: A => A): ZIO[Console, IOException, Unit] =
      r.modify { oldValue =>
        val newValue = f(oldValue)
        ((oldValue, newValue), newValue)
      }.flatMap { case (old, newVal) =>
        putStrLn(s"Last update was: [$old -> $newVal]")
      }
  }

  trait RefCache[K, V] {
    def getOrElseCompute(k: K)(f: K => V): UIO[Ref[V]]
  }

  object RefCache {

    def make[K, V]: UIO[RefCache[K, V]] =
      RefM.make(Map.empty[K, Ref[V]]).map { refM =>
        new RefCache[K, V] {
          def getOrElseCompute(k: K)(f: K => V): UIO[Ref[V]] =
            refM
              .modify { map =>
                map.get(k) match {
                  case Some(v) => UIO.effectTotal((v, map))
                  case _       => Ref.make(f(k)).map(nv => (nv, map + (k -> nv)))
                }
              }
        }
      }

  }

}

object SharedStateUnsafe extends ZIOApp {

  import SharedState._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, IOException, Unit] =
    for {
      ref   <- Var.make(0)
      _     <- ZIO.foreachPar_(1 to 10000)(_ => ref.modify(_ + 1))
      value <- ref.get
      _     <- putStrLn(s"The final value is: [$value]")
    } yield ()
}

object SharedStateSafe1 extends ZIOApp {

  import SharedState._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, IOException, Unit] =
    for {
      ref   <- Ref.make(0)
      _     <- ZIO.foreachPar_(1 to 10000)(_ => ref.update(_ + 1))
      value <- ref.get
      _     <- putStrLn(s"The final value is: [$value]")
    } yield ()
}

object SharedStateSafe2 extends ZIOApp {

  import SharedState._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, IOException, Unit] =
    for {
      ref   <- Ref.make(0)
      _     <- ZIO.foreachPar_(1 to 10000)(_ => Ref.updateAndLog(ref)(_ + 1))
      value <- ref.get
      _     <- putStrLn(s"The final value is: [$value]")
    } yield ()
}
