package zionomicon.ch19

import zio._

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
