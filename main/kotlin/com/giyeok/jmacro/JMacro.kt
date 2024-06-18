package com.giyeok.jmacro

class JMacro(val builder: StringBuilder) {
  fun write(body: JMacroAst.MacroBody, ctx: MacroCtx) {
    var currentCtx = ctx
    for (elem in body.elems) {
      currentCtx = write(elem, currentCtx)
    }
  }

  fun write(elem: JMacroAst.MacroElem, ctx: MacroCtx): MacroCtx =
    when (elem) {
      is JMacroAst.PlainSnip -> {
        builder.append(elem.snip)
        ctx
      }

      is JMacroAst.SharpEscape -> {
        builder.append('#')
        ctx
      }

      is JMacroAst.ClassDecl -> {
        // Do nothing
        ctx
      }

      is JMacroAst.ForDirective -> {
        val coll = evaluate(elem.iterOver, ctx)
        check(coll is ArrayValue) {
          ""
        }
        for (value in coll.elems) {
          val loopCtx = when (val iterName = elem.iterName) {
            is JMacroAst.ExtractTuple -> {
              when (value) {
                is TupleValue -> {
                  check(value.elems.size == iterName.elems.size)
                  ctx.withNames(iterName.elems.map { it.name }.zip(value.elems).toMap())
                }

                is ClassValue -> {
                  TODO()
                }

                else -> throw IllegalStateException("")
              }
            }

            is JMacroAst.Name -> {
              ctx.withName(iterName.name to value)
            }
          }
          write(elem.body, loopCtx)
        }
        ctx
      }

      is JMacroAst.LetDirective -> {
        val value = evaluate(elem.value, ctx)
        ctx.withName(elem.name.name to value)
      }

      is JMacroAst.NameReplace -> {
        val value = ctx.names[elem.name.name]
          ?: throw IllegalStateException("Name not found: ${elem.name.name}")
        writeValue(value, elem)
        ctx
      }

      is JMacroAst.ExprReplace -> {
        val value = evaluate(elem.expr, ctx)
        writeValue(value, elem)
        ctx
      }
    }

  fun writeValue(value: MacroValue, ast: JMacroAst.AstNode) {
    if (value !is SnipValue)
      throw IllegalStateException("Not snip $ast")
    builder.append(value.value)
  }

  companion object {
    fun evaluate(expr: JMacroAst.Expr, ctx: MacroCtx): MacroValue {
      return when (expr) {
        is JMacroAst.Call -> {
          val callee = when (val callee = expr.callee) {
            is JMacroAst.MemberAccess -> evaluate(callee, ctx)
            is JMacroAst.Name -> {
              val cls = ctx.classes[callee.name]
              if (cls != null) {
                val args = expr.args.map { evaluate(it, ctx) }
                if (cls.fields.size != args.size) {
                  throw IllegalStateException("TODO msg")
                }
                // TODO type check
                val fields = cls.fields.zip(args).associate { (field, value) ->
                  field.first to value
                }
                var currentCtx = ctx.withNames(fields)
                val elems = mutableMapOf<String, MacroValue>()
                for ((name, elemExpr) in cls.elems) {
                  val value = evaluate(elemExpr, currentCtx)
                  elems[name] = value
                  currentCtx = currentCtx.withName(name to value)
                }
                return ClassValue(fields + elems)
              }
              evaluate(expr.callee, ctx)
            }
          }
          if (callee !is FuncValue) {
            throw IllegalStateException("TODO message")
          }
          val args = expr.args.map { evaluate(it, ctx) }
          if (callee.params.size != args.size) {
            throw IllegalStateException("")
          }

          evaluate(callee.bodyExpr, callee.outerCtx.withNames(callee.params.zip(args).toMap()))
        }

        is JMacroAst.ArrayExpr -> ArrayValue(expr.elems.map { evaluate(it, ctx) })
        is JMacroAst.TupleExpr -> TupleValue(expr.elems.map { evaluate(it, ctx) })

        is JMacroAst.Lambda -> {
          val params = expr.params.map {
            when (it) {
              is JMacroAst.Name -> it.name
              else -> TODO()
            }
          }
          FuncValue(params, expr.body, ctx)
        }

        is JMacroAst.MemberAccess -> {
          val value = evaluate(expr.value, ctx)
          if (value !is ClassValue) {
            throw IllegalStateException("")
          }
          value.fields[expr.name.name]
            ?: throw IllegalStateException()
        }

        is JMacroAst.Name -> {
          ctx.names[expr.name] ?: throw IllegalStateException("Name not found: ${expr.name}")
        }

        is JMacroAst.Paren -> evaluate(expr.body, ctx)
        is JMacroAst.SnipExpr -> {
          val builder = StringBuilder()
          val macro = JMacro(builder)
          for (elem in expr.elems) {
            when (elem) {
              is JMacroAst.InnerSnipElem -> builder.append(elem.snip)
              is JMacroAst.InnerSnipEscape -> builder.append(elem.value)
              is JMacroAst.ExprReplace -> macro.write(elem, ctx)
              is JMacroAst.NameReplace -> macro.write(elem, ctx)
            }
          }
          SnipValue(builder.toString())
        }
      }
    }
  }
}