package zionomicon.ch2

import zio._
import zio.clock._
import zio.duration._

object GroceryStore extends App {

  val goShopping: Task[Unit] = ZIO.effect(println("Go to the GroceryStore"))

  val goShoppingLater: ZIO[Any with Clock, Throwable, Unit] = goShopping.delay(5.seconds)

  def run(args: List[String]) = goShoppingLater.exitCode

}
