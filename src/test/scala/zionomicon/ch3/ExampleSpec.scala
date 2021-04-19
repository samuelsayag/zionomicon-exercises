package zionomicon.ch3

import zio._
import zio.test._
import zio.test.Assertion._

object ExampleSpec extends DefaultRunnableSpec {
  def spec: Spec[Any, TestFailure[Throwable], TestSuccess] =
    suite("All suite")(firstSuite, secondSuite)

  def firstSuite: Spec[Any, TestFailure[Nothing], TestSuccess] = suite("First suite")(
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
    },
    test("test has same element") {
      assert(List(1, 2, 3))(hasSameElements(List(2, 3, 1)))
    },
    testM("failure I") {
      for {
        exit <- ZIO.effect(1 / 0).catchAll(_ => ZIO.fail(())).run
      } yield assert(exit)(fails(isUnit))
    },
    testM("failure II") {
      for {
        exit <- ZIO.effect(1 / 0).run
      } yield assert(exit)(fails(anything))
    },
    testM("failure III") {
      for {
        exit <- ZIO.effect(1 / 0).run
      } yield assert(exit)(fails(isSubtype[ArithmeticException](anything)))
    }
  )

  def secondSuite: Spec[Any, TestFailure[Throwable], TestSuccess] = suite("Second suite")(
    testM("throw ArithmeticException") {
      for {
        exit <- ZIO.effect(1 / 0)
      } yield assert(exit)(throwsA[ArithmeticException])
    }
  )

}
