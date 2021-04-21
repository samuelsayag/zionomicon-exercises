package zionomicon.ch3

import zio.test._
import zio.random._
import zio.test.Assertion._

object TestPropCheckSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("Test illustrate used of Gen and property check")(testRndIntGen, testUser)

  val intGen: Gen[Random, Int] =
    Gen.anyInt

  val testRndIntGen: ZSpec[TestConfig with Random, Nothing] = testM("Test associativity of int") {
    check(intGen, intGen, intGen) { (x, y, z) =>
      val left  = (x + y) + z
      val right = x + (y + z)
      assert(left)(equalTo(right))
    }
  }

  final case class User(name: String, age: Int)

  val genName: Gen[Random with Sized, String] =
    Gen.anyASCIIString.filterNot(_.isEmpty)

  val genInt: Gen[Random, Int] =
    Gen.int(18, 120)

  val genUser: Gen[Random with Sized, User] = for {
    name <- genName
    age  <- genInt
  } yield User(name, age)

  val testUser: ZSpec[TestConfig with Random with Sized,Nothing] = testM("Test the creation of (correct users)") {
    check(genUser) { u =>
      assert(u.name)(matchesRegex("""^\p{ASCII}+$""")) && assert(u.age)(
        isLessThan(121) && isGreaterThan(-1)
      )
    }
  }

}
