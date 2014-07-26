import scala.io._

object Main extends App {
  val lines = Source.fromFile(args(0)).getLines.toList
  println(Gccr.compile(lines))
}

object Gccr {

  type FunId = String

  def compile(prg: String): String = {
    compile(prg.split("\n").toList)
  }

  def compile(lines: List[String]): String = {
    val trimmed = lines map (_.trim.toUpperCase)

    val labels_tmp = trimmed.foldLeft((Map.empty[FunId, Int], -1)) { (stuff, next) ⇒
      val (acc, line) = stuff
      if (next startsWith "$")
        (acc.updated(next.drop(1).takeWhile(_ != ':'), line + 1), line)
      else if (next.isEmpty || next.startsWith(";"))
        (acc, line)
      else
        (acc, line + 1)
    }
    val labels = labels_tmp._1

    val addressed = trimmed map { line ⇒
      if (line startsWith "$")
        line
      else
        labels.toList.sortBy(_._1).reverse.foldLeft(line) { (line, next) ⇒
          val (label, num) = next
          line.replace("$" + label, num.toString)
        }
    }

    val commented = addressed map {
      case s if s startsWith "$" ⇒ s";$s"
      case s ⇒ s
    }

    val out = commented
    out mkString "\n"
  }
}
