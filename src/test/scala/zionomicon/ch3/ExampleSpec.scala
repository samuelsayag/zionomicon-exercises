package zionomicon.ch3

import zio._
import zio.test._
import zio.test.Assertion._

object ExampleSpec extends DefaultRunnableSpec {

  def spec = suite("ExampleSpec")(
    test("addition works") {
      assert(1 + 1)(equalTo(2))
    },
    testM("addition works also with ZIO pure value") {
      assertM(ZIO.succeed(1 + 1))(equalTo(2))
    },
    testM("addition works also with ZIO pure value II") {
      ZIO.succeed(1 + 1).map(v => assert(v)(equalTo(2)))
    },
    testM("addition works also with ZIO pure value III") {
      for {
        v <- ZIO.succeed(1 + 1)
      } yield assert(v)(equalTo(2))
    },
    testM("addition works also with ZIO pure value IV") {
      for {
        x <- ZIO.succeed(1)
        y <- ZIO.succeed(2)
      } yield assert(x)(equalTo(1)) && assert(y)(equalTo(2))
    }
  )

}
