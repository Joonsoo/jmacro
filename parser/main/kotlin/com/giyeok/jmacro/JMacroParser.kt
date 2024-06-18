package com.giyeok.jmacro

import com.giyeok.jparser.ktparser.mgroup2.MilestoneGroupParserLoader
import kotlin.io.path.Path
import kotlin.io.path.readText

object JMacroParser {
  val parser = MilestoneGroupParserLoader.loadParserFromResource("/jmacro-mg2-parserdata.pb")

  fun parse(source: String): JMacroAst.MacroBody {
    val result = parser.parse(source)
    val history = parser.kernelsHistory(result)
    return JMacroAst(source, history).matchStart()
  }
}

fun main() {
  val parsed = JMacroParser.parse(Path("examples/example.ktm").readText())
  println(parsed)
}
