package zionomicon.ch2

final case class ZIO[-R, +E, +A](run: R => Either[E, A]) { self =>

  def map[B](f: A => B): ZIO[R, E, B] =
    ZIO(r => self.run(r).map(f))

  def flatMap[R1 <: R, E1 >: E, B](f: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] =
    ZIO(r => self.run(r).fold(ZIO.fail(_), f).run(r))

  def foldM[R1 <: R, E1, B](
      failure: E => ZIO[R1, E1, B],
      success: A => ZIO[R1, E1, B]
  ): ZIO[R1, E1, B] =
    ZIO(r => self.run(r).fold(failure, success).run(r))

  def fold[B](failure: E => B, success: A => B): ZIO[R, Nothing, B] =
    ZIO(r => Right(self.run(r).fold(failure, success)))

  def provide(r: R): ZIO[Any, E, A] = ZIO(_ => self.run(r))

  def zipWith[R1 <: R, E1 >: E, B, C](that: ZIO[R1, E1, B])(f: (A, B) => C): ZIO[R1, E1, C] =
    ZIO(r =>
      for {
        a <- self.run(r)
        b <- that.run(r)
      } yield f(a, b)
    )

}

object ZIO {

  type IO[+E, +A] = ZIO[Any, E, A]

  type Task[+A] = ZIO[Any, Throwable, A]

  type RIO[-R, +A] = ZIO[R, Throwable, A]

  type UIO[A] = ZIO[Any, Nothing, A]

  type RUIO[-R, +A] = ZIO[R, Nothing, A]

  import scala.util._

  def succeed[A](a: => A): ZIO[Any, Nothing, A] = ZIO(_ => Right(a))

  def effect[A](a: => A): ZIO[Any, Throwable, A] =
    // ZIO(_ =>
    //   try { Right(a) }
    //   catch { case t: Throwable => Left(t) }
    // )
    ZIO(_ => Try(a).toEither)

  def fail[E](e: => E): ZIO[Any, E, Nothing] = ZIO(_ => Left(e))

  def environment[R](): ZIO[R, Nothing, R] = ZIO(r => Right(r))

  def collectAll[R, E, A](iterable: Iterable[ZIO[R, E, A]]): ZIO[R, E, List[A]] =
    iterable.foldLeft(ZIO.succeed(List.empty[A]): ZIO[R, E, List[A]]) { (seed, zel) =>
      for {
        el <- zel
        l  <- seed
      } yield el :: l
    }

  def foreach[R, E, A, B](in: Iterable[A])(f: A => ZIO[R, E, B]): ZIO[R, E, List[B]] =
    in.foldLeft(ZIO.succeed(List.empty[B]): ZIO[R, E, List[B]]) { (seed, el) =>
      for {
        el <- f(el)
        l  <- seed
      } yield el :: l
    }

  def orElse[R, E1, E2, A](self: ZIO[R, E1, A], that: ZIO[R, E2, A]): ZIO[R,E2,A] =
    self.foldM(_ => that, ZIO.succeed(_))

}
