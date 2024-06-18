package com.giyeok.jmacro

data class MacroCtx(
  val classes: Map<String, ClassDef>,
  val macros: Map<String, MacroDef>,
  val names: Map<String, Value>
) {
  companion object {
    fun findClassDefs(ast: JMacroAst.MacroBody): Map<String, ClassDef> {
      val clsDefs = ast.elems.filterIsInstance<JMacroAst.ClassDecl>()
      val clsNames = clsDefs.map { it.name.name }.toSet()
      return clsDefs.associate { cls ->
        val fields = cls.fields.map { param ->
          param.name.name to (param.type?.let { compileType(it, clsNames) } ?: SnipType)
        }
        val elems = cls.elems?.associate { elem ->
          elem.name.name to elem.body
        }
        val clsDef = ClassDef(cls.name.name, fields, elems ?: mapOf())
        cls.name.name to clsDef
      }
    }

    fun findMacros(clsNames: Set<String>, ast: JMacroAst.MacroBody): Map<String, MacroDef> {
      val macroDefs = ast.elems.filterIsInstance<JMacroAst.MacroDecl>()
      return macroDefs.associate { macro ->
        val params = macro.params.map { param ->
          param.name.name to (param.type?.let { compileType(it, clsNames) } ?: SnipType)
        }
        macro.name.name to MacroDef(macro.name.name, params, macro.body)
      }
    }

    fun initFor(ast: JMacroAst.MacroBody): MacroCtx {
      val cls = findClassDefs(ast)
      val macros = findMacros(cls.keys, ast)
      return MacroCtx(cls, macros, mapOf())
    }
  }

  fun withName(pair: Pair<String, Value>): MacroCtx =
    copy(names = names + pair)

  fun withNames(map: Map<String, Value>): MacroCtx =
    copy(names = names + map)
}

data class ClassDef(
  val name: String,
  val fields: List<Pair<String, MacroType>>,
  val elems: Map<String, JMacroAst.Expr>
)

data class MacroDef(
  val name: String,
  val params: List<Pair<String, MacroType>>,
  val body: JMacroAst.MacroBody
)

sealed class Value

data class ClassValue(val fields: Map<String, Value>): Value()

data class SnipValue(val value: String): Value()

data class ArrayValue(val elems: List<Value>): Value()
data class TupleValue(val elems: List<Value>): Value()

data class FuncValue(
  val params: List<String>,
  val bodyExpr: JMacroAst.Expr,
  val outerCtx: MacroCtx
): Value()

sealed class MacroType

data object SnipType: MacroType()

data class ClassType(val className: String): MacroType()

data class FuncType(val params: List<MacroType>, val returnType: MacroType): MacroType()

data class TupleType(val elems: List<MacroType>): MacroType()

data class ArrayType(val elemType: MacroType): MacroType()

fun compileType(type: JMacroAst.Type, clsNames: Set<String>): MacroType = when (type) {
  is JMacroAst.ArrayType -> ArrayType(compileType(type.elem, clsNames))
  is JMacroAst.TupleType -> TupleType(type.elems.map { compileType(it, clsNames) })
  is JMacroAst.FuncType -> FuncType(
    type.params.map { compileType(it, clsNames) },
    compileType(type.result, clsNames)
  )

  is JMacroAst.Name -> when (val name = type.name) {
    "Snip" -> SnipType
    else -> {
      if (name in clsNames) {
        ClassType(name)
      } else {
        throw IllegalStateException()
      }
    }
  }
}
