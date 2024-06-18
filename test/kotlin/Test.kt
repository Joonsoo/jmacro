package com.giyeok.jmacro

import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.readText

class Test {
  @Test
  fun test() {
    val parsed = JMacroParser.parse(
      """
        #let binOpFuncs = [
          (`HexManAst.AddOps.ADD`, (a, b) -> `#a + #b`),
          (`HexManAst.AddOps.SUB`, (a, b) -> `#a - #b`),
          (`HexManAst.AddOps.MUL`, (a, b) -> `#a * #b`),
          (`HexManAst.AddOps.DIV`, (a, b) -> `#a / #b`),
        ]
        when (expr.op) {
        #for ((i, ifunc) in binOpFuncs)
        #i -> #{ifunc(`a`, `b`)}
        #end
        }
      """.trimIndent()
    )
    val builder = StringBuilder()
    JMacro(builder).write(parsed, MacroCtx(mapOf(), mapOf()))
    println(builder.toString())
  }

  @Test
  fun test2() {
    val parsed = JMacroParser.parse(Path("examples/example.ktm").readText())
    val builder = StringBuilder()
    JMacro(builder).write(parsed, MacroCtx(MacroCtx.findClassDefs(parsed), mapOf()))
    println(builder.toString())
  }
}
