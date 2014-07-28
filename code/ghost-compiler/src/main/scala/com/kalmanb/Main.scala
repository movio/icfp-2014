package com.kalmanb

object Main extends App {
  val filename = args(0)
  val lines = scala.io.Source.fromFile(filename).getLines.toList
  val compiled = Processor.process(lines)
  println
  println(compiled)
}
