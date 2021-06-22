package zionomicon.ch19

import zio._

object chapPhase1 {
  trait Database {
    def getUserName(id: Long): UIO[String]
  }

  trait Logging {
    def logLine(s: String): UIO[Unit]
  }

  val neeedsDatabase: ZIO[Database, Nothing, String] = ???
  val neeedsLogging: ZIO[Logging, Nothing, Unit]     = ???

  // how can this compile?
  // we are intersecting 2 very different types that are not values and do not have any obvious operator
  lazy val needsBoth: ZIO[Database with Logging, Nothing, String] =
    neeedsDatabase <* neeedsLogging

  // combine the service necessitate to write boilerplate code to wire service together
  // in a no dynamic fashion
  val combinedServices: Database with Logging =
    new Database with Logging {
      override def getUserName(id: Long): UIO[String] = getUserName(id)
      override def logLine(s: String): UIO[Unit]      = logLine(s)
    }

}

object chapPhase2 {

  trait Database {
    def database: Database.Service
  }

  object Database {
    trait Service {
      def getUserName(id: Long): UIO[String]
    }
  }

  trait Logging {
    def logging: Logging.Service
  }

  object Logging {
    trait Service {
      def logLine(s: String): UIO[Unit]
    }
  }

  val combinedServices: Database with Logging =
    new Database with Logging {
      def database: Database.Service = ???
      def logging: Logging.Service   = ???
    }
}
// illustrate the best "module pattern" ~ best practice to create a module with ZIO
// that will compose with Layers.
//
// Why does the Service is definied inside and object with a Service trait?
object logging {

  // module definition
  type Logging = Has[Logging.Service]

  object Logging {

    // service definition
    trait Service {
      def log(line: String): UIO[Unit]
    }

    // service implementation
    val console: ULayer[Logging] =
      ZLayer.succeed(
        new Service {
          def log(line: String): UIO[Unit] = UIO.effectTotal(println(line))
        }
      )
  }

  // accessor for a smoother experience of the API
  def log(line: String): URIO[Logging, Unit] =
    ZIO.accessM[Logging] { logging =>
      logging.get.log(line)
    }
}

// module pattern 2.0
object logging2 {

  trait Logging {
    def log(line: String): UIO[Unit]
  }

  final case class LoggingLive() extends Logging {
    override def log(line: String): UIO[Unit] = ???

  }

  object LoggingLive {
    val layer: ULayer[Has[Logging]] = (LoggingLive.apply _).toLayer
  }

  def log(line: String): URIO[Has[Logging], Unit] = ZIO.serviceWith(s => s.log(line))
}
