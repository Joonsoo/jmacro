package com.giyeok.jmacro

data class MacroCtx(
  val classes: Map<String, MacroClass>,
  val names: Map<String, MacroValue>
) {
  companion object {
    fun findClassDefs(ast: JMacroAst.MacroBody): Map<String, MacroClass> {
      val clsDefs = ast.elems.filterIsInstance<JMacroAst.ClassDecl>()
      val clsNames = clsDefs.map { it.name.name }.toSet()
      return clsDefs.associate { cls ->
        val fields = cls.fields.map { param ->
          param.name.name to (param.type?.let { compileType(it, clsNames) } ?: SnipType)
        }
        val elems = cls.elems?.associate { elem ->
          elem.name.name to elem.body
        }
        val clsDef = MacroClass(cls.name.name, fields, elems ?: mapOf())
        cls.name.name to clsDef
      }
    }
  }

  fun withName(pair: Pair<String, MacroValue>): MacroCtx =
    MacroCtx(classes, names + pair)

  fun withNames(map: Map<String, MacroValue>): MacroCtx =
    MacroCtx(classes, names + map)
}

data class MacroClass(
  val name: String,
  val fields: List<Pair<String, MacroType>>,
  val elems: Map<String, JMacroAst.Expr>
)

sealed class MacroValue

data class ClassValue(val fields: Map<String, MacroValue>): MacroValue()

data class SnipValue(val value: String): MacroValue()

data class ArrayValue(val elems: List<MacroValue>): MacroValue()
data class TupleValue(val elems: List<MacroValue>): MacroValue()

data class FuncValue(
  val params: List<String>,
  val bodyExpr: JMacroAst.Expr,
  val outerCtx: MacroCtx
): MacroValue()

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
