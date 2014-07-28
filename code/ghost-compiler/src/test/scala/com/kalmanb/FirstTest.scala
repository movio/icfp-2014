package com.kalmanb

import org.mockito.Matchers._

import com.kalmanb.test.TestSpec

class FirstTest extends TestSpec {
  import Processor._

  /*
g=ghostXCoord
g=ghostYCoord
move(down)

jump(a=b,tag)   ; test comment
g=lastDirection
move(g)

;#tag
g=lastDirection
a=g
*/

  describe("a first test") {
    it("get x and y coords") {
      val ghc = process(s"""
g=ghostXCoord  ; test one
h=ghostYCoord  ; test two
""".lines.toSeq)

      assert(ghc == s"""
int 3  ; test one
int 5
mov g,a
int 3  ; test two
int 5
mov h,b
""")
    }

    it("should set direction") {
      val ghc = process(s"""
move(up)  ; test one
move(down)  ; test two
move(left)
move(right)
move(0)
move(1)
move(c)
move(d)
""".lines.toSeq)

      assert(ghc == s"""
mov a,0  ; test one
int 0
mov a,2  ; test two
int 0
mov a,3
int 0
mov a,1
int 0
mov a,0
int 0
mov a,1
int 0
mov a,c
int 0
mov a,d
int 0
""")
    }

    it("should get last direction") {
      val ghc = process(s"""
g=lastDirection  ; test one
""".lines.toSeq)

      assert(ghc == s"""
int 3  ; test one
int 6
mov g,b
""")
    }

    it("should support assignment") {
      val ghc = process(s"""
a=b  ; test
a=1  ; test
a=200  ; test
""".lines.toSeq)

      assert(ghc == s"""
mov a,b  ; test
mov a,1  ; test
mov a,200  ; test
""")
    }

    it("should jump to pointer") {
      val ghc = process(s"""
jump(a=b,newLocation)  ; test, one
hlt

;#otherLocation
hlt
; simple comment
;#newLocation
hlt
""".lines.toSeq)

      assert(ghc == s"""
jeq 3,a,b  ; test, one ; jump to #newLocation
hlt

;#otherLocation
hlt
; simple comment
;#newLocation
hlt
""")
    }

    it("should jump to >") {
      val ghc = process(s"""
jump(a>b,newLocation)
hlt
;#newLocation
hlt
""".lines.toSeq)

      assert(ghc == s"""
jgt 2,a,b   ; jump to #newLocation
hlt
;#newLocation
hlt
""")
    }

    it("should limit to 255 lines") {
      val lines = (1 to 200) map (_ ⇒ "a=ghostXCoord")
      val t = scala.util.Try {
        process(lines)
      }
      assert(t.isFailure == true)
    }
  }
}
