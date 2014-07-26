import org.scalatest._

class GccrTest extends FunSpec with ShouldMatchers {

  it("removes whitespace") {
    val test = Gccr.compile(
      """ldc 0
        |  ldc 1
        |
        |cons
        |rtn    """.stripMargin)
    val expected =
      """ldc 0
        |ldc 1
        |
        |cons
        |rtn""".stripMargin
    test shouldBe expected.toUpperCase
  }

  it("does not count commented lines as lines") {
    val test = Gccr.compile(
      """ldc 0
        |ldf $test
        |;comment
        |$test:
        |cons
        |rtn    """.stripMargin)
    val expected =
      """ldc 0
        |ldf 2
        |;comment
        |;$test:
        |cons
        |rtn""".stripMargin
    test shouldBe expected.toUpperCase
  }

  it("comments labels") {
    val test = Gccr.compile(
      """$step
        |ldc 0
        |ldc 1
        |cons
        |rtn""".stripMargin)
    val expected =
      """;$step
        |ldc 0
        |ldc 1
        |cons
        |rtn""".stripMargin
    test shouldBe expected.toUpperCase
  }

  it("replaces labels with the abs address") {
    val test = Gccr.compile(
      """ldc 0
        |ldf $step
        |cons
        |rtn
        |
        |$step: ;stuff
        |ldc 0
        |ldc 1
        |cons
        |rtn""".stripMargin)
    val expected =
      """ldc 0
        |ldf 4
        |cons
        |rtn
        |
        |;$step: ;stuff
        |ldc 0
        |ldc 1
        |cons
        |rtn""".stripMargin
    test shouldBe expected.toUpperCase
  }

}
