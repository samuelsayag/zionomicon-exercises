package zionomicon.ch8

import zio._
import zio.console._
import zio.duration._
import zio.{ App => ZIOApp }
import zio.clock.Clock
import java.io.IOException

object FiberSupervision {

  def dbgScope[A](scope: ZScope[A], msg: Option[String] = None): ZIO[Console, IOException, Unit] =
    (ZIO.fromOption(msg) <> ZIO.succeed("fiber scope: ")) >>= (s => putStrLn(s"$s [$scope]"))

  def dbgOpen[A](
    open: ZScope.Open[A],
    msg: Option[String] = None
  ): ZIO[Console, IOException, Unit] =
    dbgScope(open.scope, msg)
}

object FiberSuperLocalScope1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (neverEnding *> putStrLn("End of prog...") <> ZIO.unit).exitCode

  def neverEnding: ZIO[Console, IOException, Unit] = for {
    _ <- putStrLn("enter the effect")
    _ <- putStrLn("enter the never") *> ZIO.never
    // never ending
    //fiber <- putStrLn("enter the never") *> ZIO.never.fork // ending immediatly if not "joined"
    //_     <- fiber.join
  } yield ()
}

object FiberSuperLocalScope2 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = prog1.exitCode

  val childEffect: ZIO[Console with Clock, IOException, Unit] =
    putStrLn("I am the child effect").delay(1.seconds)

  val parentEffect: ZIO[Console with Clock, IOException, Fiber.Runtime[IOException, Unit]] =
    putStrLn("I am the parent effect") *> childEffect.fork
  // Put the join to see the childEffect terminating
  // >>= (e => e.join)

  val prog1: ZIO[Console with Clock, Nothing, Nothing] = parentEffect.fork *> ZIO.never
}

object FiberSuperGlobalScope extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    // first part LocalScope/GlobalScope
    // module.exitCode
    program.tap(r => putStrLn(s"Result is [$r]")).exitCode

  val effect: ZIO[Console with Clock, IOException, Int] = for {
    //_ <- putStrLn("hearbeat !").delay(1.second).forever.fork
    // when reaching the end of the LocalScope the fiber does not end
    _ <- putStrLn("hearbeat !").delay(1.second).forever.forkDaemon
    _ <- putStrLn("Doing some effect work !")
    // to see the hearbeat, else the end of the effect scope kill the fiber
    // _ <- ZIO.sleep(5.seconds)
  } yield 42

  val module: ZIO[Console with Clock, IOException, Int] = for {
    fiber  <- effect.fork
    _      <- putStrLn("Doing a lot of big work...").delay(5.seconds)
    result <- fiber.join
  } yield result

  val program: ZIO[Console with Clock, IOException, Int] = for {
    // we would like the hearbeat to live during the life of the module ONLY!
    fiber <- module.fork
    _     <- putStrLn("Doing the main work of the application").delay(10.seconds)
    res   <- fiber.join
  } yield res

}

object FiberSuperZScope1 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.tap(r => putStrLn(s"Program end with: [$r]")).exitCode

  def effect(scope: ZScope[Exit[Any, Any]]): ZIO[Console with Clock, IOException, Int] = for {
    // scope the effect in `scope`
    _ <- putStrLn("hearbeat !").delay(1.second).forever.forkIn(scope)
    _ <- putStrLn("Done with the work of the effect...")
  } yield 42

  def module(scope: ZScope[Exit[Any, Any]]): ZIO[Console with Clock, IOException, Int] = for {
    fiber  <- effect(scope).fork
    _      <- ZIO.sleep(5.seconds) *> putStrLn("Done with the work of the module...")
    result <- fiber.join
  } yield result

  val program: ZIO[Console with Clock, IOException, Int] = for {
    // create a scope for the hearbeat
    open  <- ZScope.make[Exit[Any, Any]]
    fiber <- module(open.scope).fork
    _     <- ZIO.sleep(10.seconds) *> putStrLn("Done with the work of the application...")
    res   <- fiber.join
    _     <- open.close(Exit.unit)
  } yield res

}

object FiberSuperZScope2 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.tap(r => putStrLn(s"Program end with: [$r]")).exitCode

  def effect(scope: ZScope[Exit[Any, Any]]): ZIO[Console with Clock, IOException, Int] = for {
    // scope the effect in `scope`
    _ <- putStrLn("hearbeat !").delay(1.second).forever.forkIn(scope)
    _ <- putStrLn("Done with the work of the effect...")
  } yield 42

  val module: ZIO[Console with Clock, IOException, Int] = for {
    fiber  <- ZIO.forkScopeWith(scope => effect(scope).fork)
    _      <- ZIO.sleep(5.seconds) *> putStrLn("Done with the work of the module...")
    result <- fiber.join
  } yield result

  val program: ZIO[Console with Clock, IOException, Int] = for {
    fiber <- module.fork
    _     <- ZIO.sleep(10.seconds) *> putStrLn("Done with the work of the application...")
    res   <- fiber.join
  } yield res

}

object FiberSuperZScope3 extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = ???

  // simple implementation of zipPar
  def zipPar1[R, E, A, B, C](first: ZIO[R, E, A], snd: ZIO[R, E, B])(f: (A, B) => C): ZIO[R, E, C] =
    for {
      ff <- first.fork
      fs <- snd.fork
      a  <- ff.join
      b  <- fs.join
    } yield f(a, b)

  val hearbeat: URIO[Console with Clock, Fiber.Runtime[IOException, Nothing]] =
    putStrLn("I am alive...").delay(1.second).forever.fork

  val somethingElse: ZIO[Console, IOException, Unit] = putStrLn("Something else is being done")

  val effect: ZIO[Console with Clock, IOException, Fiber.Runtime[IOException, Nothing]] =
    zipPar1(hearbeat, somethingElse)((fiber, _) => fiber)
}

object FiberSuperJustRef extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = effect.exitCode

  val effect: ZIO[Console, IOException, Unit] = for {
    ref <- Ref.make(0)
    _   <- ref.get.tap(v => putStrLn(s"Last value of the ref [$v]"))
  } yield ()
}

object FiberSuperRefFork extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (ZIO.scopeWith(s => putStrLn(s"main0: $s")) *>
      ZIO.forkScopeWith(s => putStrLn(s"main1: $s")) *>
      ZIO.forkScope.flatMap(v => putStrLn(s"main2: $v")) *>
      effect
        .tap(_ => ZIO.forkScope.flatMap(v => putStrLn(s"main3: $v")))
        .tap(v => putStrLn(s"the value is [$v]"))).exitCode

  val effect: ZIO[Console, IOException, Int] = for {
    ref   <- Ref.make(0)
    _     <- ZIO.forkScope.flatMap(v => putStrLn(s"in effect1: $v"))
    fiber <-
      ZIO
        .scopeWith(scope => putStrLn(s"during ensure: [$scope]") *> scope.ensure(_ => ref.set(5)))
        .fork
    _     <- putStrLn(s"in effect 2: [${fiber.scope}]")
    value <- ref.get
  } yield value
}

object FiberSuperRefPromise extends ZIOApp {

  def run(args: List[String]): URIO[ZEnv, ExitCode] = module.exitCode

  lazy val module: ZIO[Console, IOException, Int] = for {
    //fiber <- effect1.fork
    fiber <- effect2.fork
    ref   <- fiber.join
    value <- ref.get
    _     <- putStrLn(s"value of ref [$value]")
  } yield value

  val effect1: ZIO[Console, IOException, Ref[Int]] = for {
    promise <- Promise.make[Nothing, Unit]
    ref     <- Ref.make(0)
    _       <- (promise.succeed(()) *> ZIO.never).ensuring(ref.set(1)).fork
    _       <- promise.await
    _       <- ref.get.tap(v => putStrLn(s"value of ref [$v]"))
  } yield ref

  val effect2: ZIO[Console, IOException, Ref[Int]] = for {
    open         <- ZScope.make[Exit[Any, Any]]
    promise      <- Promise.make[Nothing, Unit]
    ref          <- Ref.make(0)
    _            <- (promise.succeed(()) *> ZIO.never).ensuring(ref.set(5)).fork
    anotherEffect = (promise.succeed(()) *> ZIO.never).ensuring(ref.set(1)).fork
    _            <- anotherEffect.overrideForkScope(open.scope)
    _            <- promise.await
    _            <- open.close(Exit.unit)
    _            <- ref.get.tap(v => putStrLn(s"value of ref [$v]"))
  } yield ref
}

object FS1 extends ZIOApp {
  import FiberSupervision._

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (ZIO.scopeWith(s => dbgScope(s)) *>
      ZIO.forkScopeWith(s => dbgScope(s))).exitCode
}

object FS2 extends ZIOApp {
  import FiberSupervision._

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (ZIO.scopeWith(s => dbgScope(s)) *>
      ZIO.forkScopeWith(s => dbgScope(s)) *>
      effect).exitCode

  val effect: ZIO[Console, IOException, Unit] =
    putStrLn("Some effect working...") *>
      ZIO.scopeWith(s => dbgScope(s)) *>
      ZIO.forkScopeWith(s => dbgScope(s)) as (())
}

object FS3 extends ZIOApp {
  import FiberSupervision._

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (ZIO.scopeWith(s => dbgScope(s)) *>
      ZIO.forkScopeWith(s => dbgScope(s)) *>
      effect *>
      ZIO.forkScopeWith(s => dbgScope(s).fork)).exitCode

  val effect: ZIO[Console, IOException, Fiber.Runtime[IOException, Unit]] =
    putStrLn("Some effect working...").fork.tap(f => dbgScope(f.scope, Some("in effect")))
}
