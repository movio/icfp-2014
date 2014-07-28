package com.kalmanb

object Processor {

  def process(ghc: Seq[String]): String = {
    val assignNumberPattern = """^.=\d+ .*;.*""".r
    val assignStringPattern = """^.=[a-h] .*;.*""".r
    val mapped = ghc flatMap (_ match {
      case s if s.contains("ghostXCoord") ⇒
        val variable = s(0)
        Seq(
          s"int 3  ${getComment(s)}",
          s"int 5",
          s"mov $variable,a"
        )
      case s if s.contains("ghostYCoord") ⇒
        val variable = s(0)
        Seq(
          s"int 3  ${getComment(s)}",
          s"int 5",
          s"mov $variable,b"
        )
      case s if s.contains("move(") ⇒ handleMove(s)
      case s if s.contains("lastDirection") ⇒ handleLastDirection(s)
      case s if s.startsWith("jump") ⇒ handleJump(s)
      case s if assignNumberPattern.findFirstMatchIn(s).isDefined ⇒ handleAssign(s)
      case s if assignStringPattern.findFirstMatchIn(s).isDefined ⇒ handleAssign(s)
      case s ⇒ Seq(s)
    })
    val jumped = finaliseJumps(mapped)
    if (getCodeLines(jumped).size > 255)
      throw new Exception()
    jumped.mkString("\n") + "\n"
  }

  def handleAssign(command: String): Seq[String] = {
    val pattern = "(.*)=(.*)".r
    val matchRes = pattern.findFirstMatchIn(command)
    matchRes match {
      case Some(m) ⇒ Seq(s"mov ${m.group(1)},${m.group(2)}")
      case _       ⇒ Seq("")
    }
  }

  def handleJump(command: String): Seq[String] = {
    val pattern = """jump\((.*)[=<>](.*),(.*)\)""".r
    val matchRes = pattern.findFirstMatchIn(command)
    val (a, b, tag) = matchRes match {
      case Some(m) ⇒ (m.group(1), m.group(2), m.group(3))
      case _       ⇒ ("", "", "")
    }
    val start = command match {
      case s if s.contains("=") ⇒ "jeq"
      case s if s.contains(">") ⇒ "jgt"
      case s if s.contains("<") ⇒ "jlt"
    }
    Seq(s"$start #$tag,$a,$b  ${getComment(command)} ; jump to #$tag")
  }

  def finaliseJumps(commands: Seq[String]) = {
    val pattern = "^j.. .*".r
    commands map (s ⇒ s match {
      case s if pattern.findFirstMatchIn(s).isDefined ⇒ replaceFirstTag(commands, s)
      case s ⇒ s
    })
  }

  def replaceFirstTag(commands: Seq[String], command: String) = {
    val pattern = """j.. #(.+),.*,[^ ]+""".r
    val matchRes = pattern.findFirstMatchIn(command)
    val tag = matchRes match {
      case Some(m) ⇒ m.group(1)
      case _       ⇒ ""
    }
    val line = getLineNumberForTag(commands, tag)
    command.replaceFirst("#[^,]*", line.toString)
  }

  def getLineNumberForTag(commands: Seq[String], tag: String) = {
    val index = commands.indexWhere(line ⇒ line.startsWith(s";#$tag"))
    val subset = commands.slice(0, index + 1)
    val code = getCodeLines(subset)
    code.size
  }

  def getCodeLines(commands: Seq[String]) = {
    val pattern = """^;.*""".r
    commands.filter(line ⇒
      !(pattern.findFirstMatchIn(line).isDefined || line.isEmpty))
  }

  def handleMove(command: String): Seq[String] = {
    val pattern = """move\(([^\)]*)\)(.*)""".r
    val matchRes = pattern.findFirstMatchIn(command)
    val (direction, comment) = matchRes match {
      case Some(m) ⇒ (m.group(1), m.group(2))
      case _       ⇒ ("", "")
    }
    val dir = direction match {
      case "up"    ⇒ "0"
      case "down"  ⇒ "2"
      case "left"  ⇒ "3"
      case "right" ⇒ "1"
      case s       ⇒ s
    }
    Seq(
      s"mov a,$dir$comment",
      s"int 0"
    )
  }

  def handleLastDirection(command: String): Seq[String] = {
    val variable = command(0)
    Seq(
      s"int 3  ${getComment(command)}",
      s"int 6",
      s"mov $variable,b"
    )
  }

  def getComment(command: String) = {
    val pattern = ".* (;.*)".r
    val matchRes = pattern.findFirstMatchIn(command)
    matchRes match {
      case Some(m) ⇒ m.group(1)
      case _       ⇒ ""
    }
  }

}
