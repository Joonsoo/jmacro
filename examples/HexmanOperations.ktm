#class ValType(name, extractFunc: (Snip) -> Snip, createFunc: (Snip) -> Snip) {
  typeName = `#{name}Type`
  constName = `#{name}Const`
}
#let valTypes: ValType[] = [
  ValType(`I8`, (x) -> `#x.value.byteValueExact()`, (x) -> `I8Const(#x.toByte())`),
  ValType(`I16`, (x) -> `#x.value.shortValueExact()`, (x) -> `I16Const(#x.toShort())`),
  ValType(`I32`, (x) -> `#x.value.intValueExact()`, (x) -> `I32Const(#x.toInt())`),
  ValType(`I64`, (x) -> `#x.value.longValueExact()`, (x) -> `I64Const(#x.toLong())`),
  ValType(`U8`, (x) -> `#x.value.ubyteValueExact()`, (x) -> `U8Const(#x.toByte().toUByte())`),
  ValType(`U16`, (x) -> `#x.value.ushortValueExact()`, (x) -> `U16Const(#x.toShort().toUShort())`),
  ValType(`U32`, (x) -> `#x.value.uintValueExact()`, (x) -> `U32Const(#x.toInt().toUInt())`),
  ValType(`U64`, (x) -> `#x.value.ulongValueExact()`, (x) -> `U64Const(#x.toLong().toULong())`),
]

#macro expandVar(lhs, rhs, types: ValType[])

#end


package com.giyeok.hexman.compiler

import com.giyeok.hexman.HexManAst
import com.giyeok.hexman.reader.*
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

fun ReaderCompiler2.Compiler.intCastStmt(
  args: HexManAst.Args,
  ctx: ReaderCompiler2.CompileCtx,
  destType: IntType
): ReaderCompiler2.ExprResult {
  check(args.args.size == 1)
  val valueExpr = args.args.first()
  check(valueExpr is HexManAst.Expr)
  val value = compileExpr(valueExpr, ctx)

  when (value.type) {
    is HType -> {
      check(value.expr is HExpr)
      compilerCheck(value.type is IntType, valueExpr) { "Invalid cast to $destType" }
      val varId = nextVarId()
      return ReaderCompiler2.ExprResult(
        value.stmts + IntCastStmt(varId, value.expr, value.type, destType),
        HVar(varId),
        destType
      )
    }

    IntLiteralType -> {
      check(value.expr is IntLiteralConst)
      return ReaderCompiler2.ExprResult(value.stmts, coerceInt(value.expr, destType), destType)
    }

    else ->
      throw CompilerException("Cannot cast non-integer value to integer", valueExpr)
  }
}

fun compareStmt(
  resultVarId: Int,
  op: HexManAst.CompareOps,
  lhsExpr: CExpr,
  lhsType: CType,
  rhsExpr: CExpr,
  rhsType: CType,
  astNode: HexManAst.AstNode
): HStmt {
  if (lhsType is IntLiteralType || rhsType is IntLiteralType) {
    return if (lhsType is IntLiteralType && rhsType is IntLiteralType) {
      // 컴파일 타임에 비교해서 boolean 값으로 넣기
      check(lhsExpr is IntLiteralConst && rhsExpr is IntLiteralConst)
      AssignStmt(resultVarId, BooleanConst(lhsExpr.value == rhsExpr.value))
    } else if (lhsType is IntLiteralType) {
      compilerCheck(rhsType is IntType, astNode) {
        "정수만 비교 가능"
      }
      check(rhsExpr is HExpr)
      CompareIntStmt(resultVarId, op, coerceInt(lhsExpr as IntLiteralConst, rhsType), rhsExpr)
    } else {
      compilerCheck(lhsType is IntType, astNode) {
        "정수만 비교 가능"
      }
      check(lhsExpr is HExpr)
      CompareIntStmt(resultVarId, op, lhsExpr, coerceInt(rhsExpr as IntLiteralConst, lhsType))
    }
  }

  compilerCheck(lhsType !is NullableType && rhsType !is NullableType, astNode) {
    "Cannot compare nullable values"
  }
  compilerCheck(lhsType == rhsType, astNode) {
    "Cannot compare the values of different types"
  }
  compilerCheck(lhsType is IntType, astNode) {
    "Only integer types can be compared"
  }
  check(lhsExpr is HExpr)
  check(rhsExpr is HExpr)
  return CompareIntStmt(resultVarId, op, lhsExpr, rhsExpr)
}

fun equalStmt(
  resultVarId: Int,
  op: HexManAst.EqualOps,
  lhsExpr: CExpr,
  lhsType: CType,
  rhsExpr: CExpr,
  rhsType: CType,
  astNode: HexManAst.AstNode,
): HStmt {
  if (lhsType is IntLiteralType || rhsType is IntLiteralType) {
    return if (lhsType is IntLiteralType && rhsType is IntLiteralType) {
      // 컴파일 타임에 비교해서 boolean 값으로 넣기
      TODO()
    } else if (lhsType is IntLiteralType) {
      compilerCheck(rhsType is IntType, astNode) {
        "정수는 정수하고만 비교 가능"
      }
      check(rhsExpr is HExpr)
      IntEqualityOpStmt(
        resultVarId,
        op,
        coerceInt(lhsExpr as IntLiteralConst, rhsType),
        rhsExpr
      )
    } else {
      compilerCheck(lhsType is IntType, astNode) {
        "정수는 정수하고만 비교 가능"
      }
      check(lhsExpr is HExpr)
      IntEqualityOpStmt(
        resultVarId,
        op,
        lhsExpr,
        coerceInt(rhsExpr as IntLiteralConst, lhsType)
      )
    }
  }

  if (lhsExpr == NullConst || rhsExpr == NullConst) {
    return when {
      lhsExpr == NullConst && rhsExpr == NullConst ->
        AssignStmt(resultVarId, BooleanConst(op == HexManAst.EqualOps.EQ))

      lhsExpr == NullConst -> {
        compilerCheck(rhsExpr is HExpr, astNode) {
          "정수와 null 비교 불가"
        }
        CheckNullStmt(resultVarId, rhsExpr, op == HexManAst.EqualOps.NE)
      }

      else -> {
        compilerCheck(lhsExpr is HExpr, astNode) {
          "정수와 null 비교 불가"
        }
        CheckNullStmt(resultVarId, lhsExpr, op == HexManAst.EqualOps.NE)
      }
    }
  }

  compilerCheck(lhsType == rhsType, astNode) { "Type mismatch: $lhsType, $rhsType" }
  check(lhsExpr is HExpr)
  check(rhsExpr is HExpr)
  return when {
    lhsType is IntType -> IntEqualityOpStmt(resultVarId, op, lhsExpr, rhsExpr)
    lhsType is ArrayType && lhsType.elemType == U8Type ->
      BytesEqualityOpStmt(resultVarId, op, lhsExpr, rhsExpr)

    lhsType is ArrayType ->
      ArrayEqualityOpStmt(resultVarId, op, lhsExpr, rhsExpr)

    lhsType is EnumType -> EnumEqualityOpStmt(resultVarId, op, lhsExpr, rhsExpr)

    else -> throw IllegalStateException("Unsupported equal comparison")
  }
}

fun ReaderCompiler2.Compiler.inCheckStmt(
  resultVarId: Int,
  op: HexManAst.InOps,
  lhsExpr: CExpr,
  lhsType: CType,
  rhsExpr: CExpr,
  rhsType: CType,
  astNode: HexManAst.AstNode,
): List<HStmt> = when (rhsType) {
  is RangeType -> {
    compilerCheck(lhsType is IntType && rhsType.elemType == lhsType, astNode) {
      "in check assertion의 우현과 좌현 타입 불일치"
    }
    check(rhsExpr is HExpr)
    listOf(IntInCheckStmt(resultVarId, op, coerce(lhsExpr, rhsType.elemType), rhsExpr))
  }

  is RangeLiteralType -> {
    check(rhsExpr is RangeLiteralConst)
    if (lhsType is IntLiteralType) {
      // compile time에 확인 가능한 경우(e.g. 1 in 1..5)
      check(lhsExpr is IntLiteralConst)
      val result = if (rhsExpr.endInclusive) {
        lhsExpr.value in rhsExpr.start..rhsExpr.end
      } else {
        rhsExpr.start <= lhsExpr.value && lhsExpr.value < rhsExpr.end
      }
      listOf(AssignStmt(resultVarId, BooleanConst(result)))
    } else {
      compilerCheck(lhsType is IntType, astNode) { "int가 아닌 값을 in range check 시도" }
      check(lhsExpr is HExpr)
      val start = coerceInt(IntLiteralConst(rhsExpr.start), lhsType)
      val end = coerceInt(IntLiteralConst(rhsExpr.end), lhsType)
      val rangeVar = nextVarId()
      listOf(
        RangeStmt(rangeVar, start, end, rhsExpr.endInclusive),
        IntInCheckStmt(resultVarId, op, lhsExpr, HVar(rangeVar))
      )
    }
  }

  is ArrayType -> {
    compilerCheck(rhsType.elemType == lhsType, astNode) { "Incosistent in check operator" }
    check(rhsExpr is HExpr)
    listOf(ArrayElemCheckStmt(resultVarId, op, coerce(lhsExpr, rhsType.elemType), rhsExpr))
  }

  else -> throw IllegalStateException("Unsupported in operator")
}

fun ReaderCompiler2.Compiler.compileAdditive(
  expr: HexManAst.Additive,
  ctx: ReaderCompiler2.CompileCtx
): ReaderCompiler2.ExprResult {
  #let additiveOps = [
    (`HexManAst.AddOps.ADD`, (a, b) -> `#a + #b`),
    (`HexManAst.AddOps.SUB`, (a, b) -> `#a - #b`)]
  val lhs = compileExpr(expr.lhs, ctx)
  val rhs = compileExpr(expr.rhs, ctx)
  compilerCheck(lhs.type !is NullableType && rhs.type !is NullableType, expr) {
    "Additive operation cannot be done on nullable values"
  }
  if (lhs.type is IntLiteralType || rhs.type is IntLiteralType) {
    when {
      lhs.type is IntLiteralType && rhs.type is IntLiteralType -> {
        check(lhs.expr is IntLiteralConst && rhs.expr is IntLiteralConst)
        val result = when (expr.op) {
          HexManAst.AddOps.ADD -> lhs.expr.value + rhs.expr.value
          HexManAst.AddOps.SUB -> lhs.expr.value - rhs.expr.value
        }
        return ReaderCompiler2.ExprResult(
          lhs.stmts + rhs.stmts,
          IntLiteralConst(result),
          IntLiteralType
        )
      }

      lhs.type is IntLiteralType -> {
        compilerCheck(rhs.type is IntType, expr) { "message: TODO" }
        check(lhs.expr is IntLiteralConst && rhs.expr is HExpr)
        return if (rhs.expr is IntConst) {
          val value = when (expr.op) {
          #for ((op, opfunc) in [(`HexManAst.AddOps.ADD`, (a, b) -> `#a + #b`), (`HexManAst.AddOps.SUB`, (a, b) -> `#a - #b`)])
          #op -> when(rhs.type) {
            #for (typ in valTypes)
            #{typ.typeName} -> #{typ.createFunc(opfunc(typ.extractFunc(`lhs.expr`), typ.extractFunc(`(rhs.expr as #{typ.constName})`)))}
            #end
          }
          #end
          }
            when (expr.op) {
              HexManAst.AddOps.ADD -> when (rhs.type) {
                I8Type -> I8Const((lhs.expr.value.byteValueExact() + (rhs.expr as I8Const).value).toByte())
                I16Type -> I16Const((lhs.expr.value.shortValueExact() + (rhs.expr as I16Const).value).toShort())
                I32Type -> I32Const((lhs.expr.value.intValueExact() + (rhs.expr as I32Const).value))
                I64Type -> I64Const((lhs.expr.value.longValueExact() + (rhs.expr as I64Const).value))
                U8Type -> U8Const(
                  (lhs.expr.value.ubyteValueExact() + (rhs.expr as U8Const).value).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  (lhs.expr.value.ushortValueExact() + (rhs.expr as U16Const).value).toShort()
                    .toUShort()
                )

                U32Type -> U32Const((lhs.expr.value.uintValueExact() + (rhs.expr as U32Const).value).toUInt())
                U64Type -> U64Const((lhs.expr.value.ulongValueExact() + (rhs.expr as U64Const).value).toULong())
              }

              HexManAst.AddOps.SUB -> when (rhs.type) {
                I8Type -> I8Const((lhs.expr.value.byteValueExact() - (rhs.expr as I8Const).value).toByte())
                I16Type -> I16Const((lhs.expr.value.shortValueExact() - (rhs.expr as I16Const).value).toShort())
                I32Type -> I32Const((lhs.expr.value.intValueExact() - (rhs.expr as I32Const).value))
                I64Type -> I64Const((lhs.expr.value.longValueExact() - (rhs.expr as I64Const).value))
                U8Type -> U8Const(
                  (lhs.expr.value.ubyteValueExact() - (rhs.expr as U8Const).value).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  (lhs.expr.value.ushortValueExact() - (rhs.expr as U16Const).value).toShort()
                    .toUShort()
                )

                U32Type -> U32Const((lhs.expr.value.uintValueExact() - (rhs.expr as U32Const).value).toUInt())
                U64Type -> U64Const((lhs.expr.value.ulongValueExact() - (rhs.expr as U64Const).value).toULong())
              }
            }
          ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, rhs.type)
        } else {
          val id = nextVarId()
          ReaderCompiler2.ExprResult(
            lhs.stmts + rhs.stmts +
              AdditiveOpStmt(id, expr.op, coerceInt(lhs.expr, rhs.type), rhs.expr),
            HVar(id),
            rhs.type
          )
        }
      }

      else -> {
        check(rhs.type is IntLiteralType)
        check(rhs.expr is IntLiteralConst)
        compilerCheck(lhs.type is IntType, expr) { "message: TODO" }
        check(lhs.expr is HExpr)
        return if (lhs.expr is IntConst) {
          val value =
            when (expr.op) {
              HexManAst.AddOps.ADD -> when (lhs.type) {
                I8Type -> I8Const(((lhs.expr as I8Const).value + rhs.expr.value.byteValueExact()).toByte())
                I16Type -> I16Const(((lhs.expr as I16Const).value + rhs.expr.value.shortValueExact()).toShort())
                I32Type -> I32Const(((lhs.expr as I32Const).value + rhs.expr.value.intValueExact()))
                I64Type -> I64Const(((lhs.expr as I64Const).value + rhs.expr.value.longValueExact()))
                U8Type -> U8Const(
                  ((lhs.expr as U8Const).value + rhs.expr.value.ubyteValueExact()).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  ((lhs.expr as U16Const).value + rhs.expr.value.ushortValueExact()).toShort()
                    .toUShort()
                )

                U32Type -> U32Const(((lhs.expr as U32Const).value + rhs.expr.value.uintValueExact()).toUInt())
                U64Type -> U64Const(((lhs.expr as U64Const).value + rhs.expr.value.ulongValueExact()).toULong())
              }

              HexManAst.AddOps.SUB -> when (lhs.type) {
                I8Type -> I8Const(((lhs.expr as I8Const).value - rhs.expr.value.byteValueExact()).toByte())
                I16Type -> I16Const(((lhs.expr as I16Const).value - rhs.expr.value.shortValueExact()).toShort())
                I32Type -> I32Const(((lhs.expr as I32Const).value - rhs.expr.value.intValueExact()))
                I64Type -> I64Const(((lhs.expr as I64Const).value - rhs.expr.value.longValueExact()))
                U8Type -> U8Const(
                  ((lhs.expr as U8Const).value - rhs.expr.value.ubyteValueExact()).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  ((lhs.expr as U16Const).value - rhs.expr.value.ushortValueExact()).toShort()
                    .toUShort()
                )

                U32Type -> U32Const(((lhs.expr as U32Const).value - rhs.expr.value.uintValueExact()).toUInt())
                U64Type -> U64Const(((lhs.expr as U64Const).value - rhs.expr.value.ulongValueExact()).toULong())
              }
            }
          ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, lhs.type)
        } else {
          val id = nextVarId()
          ReaderCompiler2.ExprResult(
            lhs.stmts + rhs.stmts +
              AdditiveOpStmt(id, expr.op, lhs.expr, coerceInt(rhs.expr, lhs.type)),
            HVar(id),
            lhs.type
          )
        }
      }
    }
  }
  compilerCheck(lhs.type is IntType && rhs.type is IntType, expr) {
    "Additive op can take integer types only ${lhs.type}, ${rhs.type}"
  }
  compilerCheck(lhs.type == rhs.type, expr) {
    "Inconsistent operand types on additive operator: ${lhs.type} and ${rhs.type}"
  }
  return if (lhs.expr is IntConst && rhs.expr is IntConst) {
    val value =
      when (expr.op) {
        HexManAst.AddOps.ADD -> when (lhs.type) {
          I8Type -> I8Const(((lhs.expr as I8Const).value + (rhs.expr as I8Const).value).toByte())
          I16Type -> I16Const(((lhs.expr as I16Const).value + (rhs.expr as I16Const).value).toShort())
          I32Type -> I32Const(((lhs.expr as I32Const).value + (rhs.expr as I32Const).value))
          I64Type -> I64Const(((lhs.expr as I64Const).value + (rhs.expr as I64Const).value))
          U8Type -> U8Const(
            ((lhs.expr as U8Const).value + (rhs.expr as U8Const).value).toByte().toUByte()
          )

          U16Type -> U16Const(
            ((lhs.expr as U16Const).value + (rhs.expr as U16Const).value).toShort().toUShort()
          )

          U32Type -> U32Const(((lhs.expr as U32Const).value + (rhs.expr as U32Const).value).toUInt())
          U64Type -> U64Const(((lhs.expr as U64Const).value + (rhs.expr as U64Const).value).toULong())
        }

        HexManAst.AddOps.SUB -> when (lhs.type) {
          I8Type -> I8Const(((lhs.expr as I8Const).value - (rhs.expr as I8Const).value).toByte())
          I16Type -> I16Const(((lhs.expr as I16Const).value - (rhs.expr as I16Const).value).toShort())
          I32Type -> I32Const(((lhs.expr as I32Const).value - (rhs.expr as I32Const).value))
          I64Type -> I64Const(((lhs.expr as I64Const).value - (rhs.expr as I64Const).value))
          U8Type -> U8Const(
            ((lhs.expr as U8Const).value - (rhs.expr as U8Const).value).toByte().toUByte()
          )

          U16Type -> U16Const(
            ((lhs.expr as U16Const).value - (rhs.expr as U16Const).value).toShort().toUShort()
          )

          U32Type -> U32Const(((lhs.expr as U32Const).value - (rhs.expr as U32Const).value).toUInt())
          U64Type -> U64Const(((lhs.expr as U64Const).value - (rhs.expr as U64Const).value).toULong())
        }
      }
    ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, lhs.type)
  } else {
    check(lhs.expr is HExpr && rhs.expr is HExpr)
    val id = nextVarId()
    ReaderCompiler2.ExprResult(
      lhs.stmts + rhs.stmts + AdditiveOpStmt(id, expr.op, lhs.expr, rhs.expr),
      HVar(id),
      lhs.type
    )
  }
}

fun ReaderCompiler2.Compiler.compileMultiplicative(
  expr: HexManAst.MultiplicativeOp,
  ctx: ReaderCompiler2.CompileCtx
): ReaderCompiler2.ExprResult {
  #let multiplicativeOps = [
    (`HexManAst.MulOps.MUL`, (a, b) -> `#a + #b`),
    (`HexManAst.MulOps.DIV`, (a, b) -> `#a - #b`),
    (`HexManAst.MulOps.REM`, (a, b) -> `#a % #b`)]
  val lhs = compileExpr(expr.lhs, ctx)
  val rhs = compileExpr(expr.rhs, ctx)
  compilerCheck(lhs.type !is NullableType && rhs.type !is NullableType, expr) {
    "Multiplicative operation cannot be done on nullable values"
  }
  if (lhs.type is IntLiteralType || rhs.type is IntLiteralType) {
    when {
      lhs.type is IntLiteralType && rhs.type is IntLiteralType -> {
        check(lhs.expr is IntLiteralConst && rhs.expr is IntLiteralConst)
        val result = when (expr.op) {
          HexManAst.MulOps.MUL -> lhs.expr.value * rhs.expr.value
          HexManAst.MulOps.DIV -> lhs.expr.value / rhs.expr.value
          HexManAst.MulOps.REM -> lhs.expr.value % rhs.expr.value
        }
        return ReaderCompiler2.ExprResult(
          lhs.stmts + rhs.stmts,
          IntLiteralConst(result),
          IntLiteralType
        )
      }

      lhs.type is IntLiteralType -> {
        compilerCheck(rhs.type is IntType, expr) { "message: TODO" }
        check(lhs.expr is IntLiteralConst && rhs.expr is HExpr)
        return if (rhs.expr is IntConst) {
          val value =
            when (expr.op) {
              HexManAst.MulOps.MUL -> when (rhs.type) {
                I8Type -> I8Const((lhs.expr.value.byteValueExact() * (rhs.expr as I8Const).value).toByte())
                I16Type -> I16Const((lhs.expr.value.shortValueExact() * (rhs.expr as I16Const).value).toShort())
                I32Type -> I32Const((lhs.expr.value.intValueExact() * (rhs.expr as I32Const).value))
                I64Type -> I64Const((lhs.expr.value.longValueExact() * (rhs.expr as I64Const).value))
                U8Type -> U8Const(
                  (lhs.expr.value.ubyteValueExact() * (rhs.expr as U8Const).value).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  (lhs.expr.value.ushortValueExact() * (rhs.expr as U16Const).value).toShort()
                    .toUShort()
                )

                U32Type -> U32Const((lhs.expr.value.uintValueExact() * (rhs.expr as U32Const).value).toUInt())
                U64Type -> U64Const((lhs.expr.value.ulongValueExact() * (rhs.expr as U64Const).value).toULong())
              }

              HexManAst.MulOps.DIV -> when (rhs.type) {
                I8Type -> I8Const((lhs.expr.value.byteValueExact() / (rhs.expr as I8Const).value).toByte())
                I16Type -> I16Const((lhs.expr.value.shortValueExact() / (rhs.expr as I16Const).value).toShort())
                I32Type -> I32Const((lhs.expr.value.intValueExact() / (rhs.expr as I32Const).value))
                I64Type -> I64Const((lhs.expr.value.longValueExact() / (rhs.expr as I64Const).value))
                U8Type -> U8Const(
                  (lhs.expr.value.ubyteValueExact() / (rhs.expr as U8Const).value).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  (lhs.expr.value.ushortValueExact() / (rhs.expr as U16Const).value).toShort()
                    .toUShort()
                )

                U32Type -> U32Const((lhs.expr.value.uintValueExact() / (rhs.expr as U32Const).value).toUInt())
                U64Type -> U64Const((lhs.expr.value.ulongValueExact() / (rhs.expr as U64Const).value).toULong())
              }

              HexManAst.MulOps.REM -> when (rhs.type) {
                I8Type -> I8Const((lhs.expr.value.byteValueExact() % (rhs.expr as I8Const).value).toByte())
                I16Type -> I16Const((lhs.expr.value.shortValueExact() % (rhs.expr as I16Const).value).toShort())
                I32Type -> I32Const((lhs.expr.value.intValueExact() % (rhs.expr as I32Const).value))
                I64Type -> I64Const((lhs.expr.value.longValueExact() % (rhs.expr as I64Const).value))
                U8Type -> U8Const(
                  (lhs.expr.value.ubyteValueExact() % (rhs.expr as U8Const).value).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  (lhs.expr.value.ushortValueExact() % (rhs.expr as U16Const).value).toShort()
                    .toUShort()
                )

                U32Type -> U32Const((lhs.expr.value.uintValueExact() % (rhs.expr as U32Const).value).toUInt())
                U64Type -> U64Const((lhs.expr.value.ulongValueExact() % (rhs.expr as U64Const).value).toULong())
              }
            }
          ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, rhs.type)
        } else {
          val id = nextVarId()
          ReaderCompiler2.ExprResult(
            lhs.stmts + rhs.stmts + MultiplicativeOpStmt(
              id,
              expr.op,
              coerceInt(lhs.expr, rhs.type),
              rhs.expr
            ),
            HVar(id),
            rhs.type
          )
        }
      }

      else -> {
        check(rhs.type is IntLiteralType)
        check(rhs.expr is IntLiteralConst)
        compilerCheck(lhs.type is IntType, expr) { "message: TODO" }
        check(lhs.expr is HExpr)
        return if (lhs.expr is IntConst) {
          val value =
            when (expr.op) {
              HexManAst.MulOps.MUL -> when (lhs.type) {
                I8Type -> I8Const(((lhs.expr as I8Const).value * rhs.expr.value.byteValueExact()).toByte())
                I16Type -> I16Const(((lhs.expr as I16Const).value * rhs.expr.value.shortValueExact()).toShort())
                I32Type -> I32Const(((lhs.expr as I32Const).value * rhs.expr.value.intValueExact()))
                I64Type -> I64Const(((lhs.expr as I64Const).value * rhs.expr.value.longValueExact()))
                U8Type -> U8Const(
                  ((lhs.expr as U8Const).value * rhs.expr.value.ubyteValueExact()).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  ((lhs.expr as U16Const).value * rhs.expr.value.ushortValueExact()).toShort()
                    .toUShort()
                )

                U32Type -> U32Const(((lhs.expr as U32Const).value * rhs.expr.value.uintValueExact()).toUInt())
                U64Type -> U64Const(((lhs.expr as U64Const).value * rhs.expr.value.ulongValueExact()).toULong())
              }

              HexManAst.MulOps.DIV -> when (lhs.type) {
                I8Type -> I8Const(((lhs.expr as I8Const).value / rhs.expr.value.byteValueExact()).toByte())
                I16Type -> I16Const(((lhs.expr as I16Const).value / rhs.expr.value.shortValueExact()).toShort())
                I32Type -> I32Const(((lhs.expr as I32Const).value / rhs.expr.value.intValueExact()))
                I64Type -> I64Const(((lhs.expr as I64Const).value / rhs.expr.value.longValueExact()))
                U8Type -> U8Const(
                  ((lhs.expr as U8Const).value / rhs.expr.value.ubyteValueExact()).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  ((lhs.expr as U16Const).value / rhs.expr.value.ushortValueExact()).toShort()
                    .toUShort()
                )

                U32Type -> U32Const(((lhs.expr as U32Const).value / rhs.expr.value.uintValueExact()).toUInt())
                U64Type -> U64Const(((lhs.expr as U64Const).value / rhs.expr.value.ulongValueExact()).toULong())
              }

              HexManAst.MulOps.REM -> when (lhs.type) {
                I8Type -> I8Const(((lhs.expr as I8Const).value % rhs.expr.value.byteValueExact()).toByte())
                I16Type -> I16Const(((lhs.expr as I16Const).value % rhs.expr.value.shortValueExact()).toShort())
                I32Type -> I32Const(((lhs.expr as I32Const).value % rhs.expr.value.intValueExact()))
                I64Type -> I64Const(((lhs.expr as I64Const).value % rhs.expr.value.longValueExact()))
                U8Type -> U8Const(
                  ((lhs.expr as U8Const).value % rhs.expr.value.ubyteValueExact()).toByte()
                    .toUByte()
                )

                U16Type -> U16Const(
                  ((lhs.expr as U16Const).value % rhs.expr.value.ushortValueExact()).toShort()
                    .toUShort()
                )

                U32Type -> U32Const(((lhs.expr as U32Const).value % rhs.expr.value.uintValueExact()).toUInt())
                U64Type -> U64Const(((lhs.expr as U64Const).value % rhs.expr.value.ulongValueExact()).toULong())
              }
            }
          ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, lhs.type)
        } else {
          val id = nextVarId()
          ReaderCompiler2.ExprResult(
            lhs.stmts + rhs.stmts +
              MultiplicativeOpStmt(id, expr.op, lhs.expr, coerceInt(rhs.expr, lhs.type)),
            HVar(id),
            lhs.type
          )
        }
      }
    }
  }
  compilerCheck(lhs.type is IntType && rhs.type is IntType, expr) {
    "Multiplicative op can take integer types only ${lhs.type}, ${rhs.type}"
  }
  compilerCheck(lhs.type == rhs.type, expr) {
    "Inconsistent operand types on multiplicative operator: ${lhs.type} and ${rhs.type}"
  }
  return if (lhs.expr is IntConst && rhs.expr is IntConst) {
    val value =
      when (expr.op) {
        HexManAst.MulOps.MUL -> when (lhs.type) {
          I8Type -> I8Const(((lhs.expr as I8Const).value * (rhs.expr as I8Const).value).toByte())
          I16Type -> I16Const(((lhs.expr as I16Const).value * (rhs.expr as I16Const).value).toShort())
          I32Type -> I32Const(((lhs.expr as I32Const).value * (rhs.expr as I32Const).value))
          I64Type -> I64Const(((lhs.expr as I64Const).value * (rhs.expr as I64Const).value))
          U8Type -> U8Const(
            ((lhs.expr as U8Const).value * (rhs.expr as U8Const).value).toByte().toUByte()
          )

          U16Type -> U16Const(
            ((lhs.expr as U16Const).value * (rhs.expr as U16Const).value).toShort().toUShort()
          )

          U32Type -> U32Const(((lhs.expr as U32Const).value * (rhs.expr as U32Const).value).toUInt())
          U64Type -> U64Const(((lhs.expr as U64Const).value * (rhs.expr as U64Const).value).toULong())
        }

        HexManAst.MulOps.DIV -> when (lhs.type) {
          I8Type -> I8Const(((lhs.expr as I8Const).value / (rhs.expr as I8Const).value).toByte())
          I16Type -> I16Const(((lhs.expr as I16Const).value / (rhs.expr as I16Const).value).toShort())
          I32Type -> I32Const(((lhs.expr as I32Const).value / (rhs.expr as I32Const).value))
          I64Type -> I64Const(((lhs.expr as I64Const).value / (rhs.expr as I64Const).value))
          U8Type -> U8Const(
            ((lhs.expr as U8Const).value / (rhs.expr as U8Const).value).toByte().toUByte()
          )

          U16Type -> U16Const(
            ((lhs.expr as U16Const).value / (rhs.expr as U16Const).value).toShort().toUShort()
          )

          U32Type -> U32Const(((lhs.expr as U32Const).value / (rhs.expr as U32Const).value).toUInt())
          U64Type -> U64Const(((lhs.expr as U64Const).value / (rhs.expr as U64Const).value).toULong())
        }

        HexManAst.MulOps.REM -> when (lhs.type) {
          I8Type -> I8Const(((lhs.expr as I8Const).value % (rhs.expr as I8Const).value).toByte())
          I16Type -> I16Const(((lhs.expr as I16Const).value % (rhs.expr as I16Const).value).toShort())
          I32Type -> I32Const(((lhs.expr as I32Const).value % (rhs.expr as I32Const).value))
          I64Type -> I64Const(((lhs.expr as I64Const).value % (rhs.expr as I64Const).value))
          U8Type -> U8Const(
            ((lhs.expr as U8Const).value % (rhs.expr as U8Const).value).toByte().toUByte()
          )

          U16Type -> U16Const(
            ((lhs.expr as U16Const).value % (rhs.expr as U16Const).value).toShort().toUShort()
          )

          U32Type -> U32Const(((lhs.expr as U32Const).value % (rhs.expr as U32Const).value).toUInt())
          U64Type -> U64Const(((lhs.expr as U64Const).value % (rhs.expr as U64Const).value).toULong())
        }
      }
    ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, lhs.type)
  } else {
    check(lhs.expr is HExpr && rhs.expr is HExpr)
    val id = nextVarId()
    ReaderCompiler2.ExprResult(
      lhs.stmts + rhs.stmts + MultiplicativeOpStmt(id, expr.op, lhs.expr, rhs.expr),
      HVar(id),
      lhs.type
    )
  }
}

fun ReaderCompiler2.Compiler.compileBitwise(
  expr: HexManAst.Bitwise,
  ctx: ReaderCompiler2.CompileCtx
): ReaderCompiler2.ExprResult {
  val lhs = compileExpr(expr.lhs, ctx)
  val rhs = compileExpr(expr.rhs, ctx)
  compilerCheck(lhs.type !is NullableType && rhs.type !is NullableType, expr) {
    "Bitwise operation cannot be done on nullable values"
  }
  #let bitwiseOps = [
    (`HexManAst.BitwiseOps.BITWISE_AND`, (a, b) -> `#a and #b`),
    (`HexManAst.BitwiseOps.BITWISE_OR`, (a, b) -> `#a and #b`),
    (`HexManAst.BitwiseOps.BITWISE_XOR`, (a, b) -> `#a and #b`)]
  if (lhs.type is IntLiteralType || rhs.type is IntLiteralType) {
    when {
      lhs.type is IntLiteralType && rhs.type is IntLiteralType -> {
        check(lhs.expr is IntLiteralConst && rhs.expr is IntLiteralConst)
        val lhsVal = lhs.expr.value.intValueExact()
        val rhsVal = rhs.expr.value.intValueExact()
        val result = when (expr.op) {
          HexManAst.BitwiseOps.BITWISE_AND -> lhsVal and rhsVal
          HexManAst.BitwiseOps.BITWISE_OR -> lhsVal or rhsVal
          HexManAst.BitwiseOps.BITWISE_XOR -> lhsVal xor rhsVal
        }
        return ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, I32Const(result), IntLiteralType)
      }

      lhs.type is IntLiteralType -> {
        compilerCheck(rhs.type is IntType, expr) { "message: TODO" }
        check(lhs.expr is IntLiteralConst && rhs.expr is HExpr)
        return if (rhs.expr is IntConst) {
          val value = when (expr.op) {
            #for ((op, opfunc) in bitwiseOps)
              #op -> when (lhs.type) {
                #for (typ in valTypes)
                #{typ.typeName} -> #{typ.createFunc(opfunc(typ.extractFunc(`lhs.expr`), `(rhs.expr as #{typ.constName}).value`))}
                #end
              }
            #end
          }
          ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, rhs.type)
        } else {
          val id = nextVarId()
          ReaderCompiler2.ExprResult(
            lhs.stmts + rhs.stmts + BitwiseOpStmt(
              id,
              expr.op,
              coerceInt(lhs.expr, rhs.type),
              rhs.expr
            ),
            HVar(id),
            rhs.type
          )
        }
      }

      else -> {
        check(rhs.type is IntLiteralType)
        check(rhs.expr is IntLiteralConst)
        compilerCheck(lhs.type is IntType, expr) { "message: TODO" }
        check(lhs.expr is HExpr)
        return if (lhs.expr is IntConst) {
          val value = when (expr.op) {
            #for ((op, opfunc) in bitwiseOps)
              #op -> when (lhs.type) {
                #for (typ in valTypes)
                #{typ.typeName} -> #{typ.createFunc(opfunc(`(lhs.expr as #{typ.constName}).value`, typ.extractFunc(`rhs.expr`)))}
                #end
              }
            #end
          }
          ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, lhs.type)
        } else {
          val id = nextVarId()
          ReaderCompiler2.ExprResult(
            lhs.stmts + rhs.stmts + BitwiseOpStmt(
              id,
              expr.op,
              lhs.expr,
              coerceInt(rhs.expr, lhs.type)
            ),
            HVar(id),
            lhs.type
          )
        }
      }
    }
  }
  compilerCheck(lhs.type is IntType && rhs.type is IntType, expr) {
    "Bitwise op can take integer types only ${lhs.type}, ${rhs.type}"
  }
  compilerCheck(lhs.type == rhs.type, expr) {
    "Inconsistent operand types on bitwise operator: ${lhs.type} and ${rhs.type}"
  }
  return if (lhs.expr is IntConst && rhs.expr is IntConst) {
    val value = when (expr.op) {
      #for ((op, opfunc) in bitwiseOps)
        #op -> when (lhs.type) {
          #for (typ in valTypes)
          #{typ.typeName} -> #{typ.createFunc(opfunc(`(lhs.expr as #{typ.constName}).value`, `(rhs.expr as #{typ.constName}).value`))}
          #end
        }
      #end
    }
    ReaderCompiler2.ExprResult(lhs.stmts + rhs.stmts, value, lhs.type)
  } else {
    check(lhs.expr is HExpr && rhs.expr is HExpr)
    val id = nextVarId()
    ReaderCompiler2.ExprResult(
      lhs.stmts + rhs.stmts + BitwiseOpStmt(id, expr.op, lhs.expr, rhs.expr),
      HVar(id),
      lhs.type
    )
  }
}

fun ReaderCompiler2.Compiler.compileUnary(
  expr: HexManAst.UnaryOp,
  ctx: ReaderCompiler2.CompileCtx
): ReaderCompiler2.ExprResult {
  val operand = compileExpr(expr.operand, ctx)
  compilerCheck(operand.type !is NullableType, expr) {
    "Unary operator cannot be used on nullable values"
  }

  if (operand.type is IntLiteralType) {
    check(operand.expr is IntLiteralConst)
    val value: CExpr = when (expr.op) {
      HexManAst.UnaryOps.MINUS -> IntLiteralConst(-operand.expr.value)
      HexManAst.UnaryOps.PLUS -> operand.expr
      HexManAst.UnaryOps.TILDE -> I32Const(operand.expr.value.intValueExact().inv())
      HexManAst.UnaryOps.NEG -> throw CompilerException("Cannot negate an integer", expr)
    }
    return ReaderCompiler2.ExprResult(operand.stmts, value, IntLiteralType)
  }

  when (expr.op) {
    HexManAst.UnaryOps.PLUS, HexManAst.UnaryOps.MINUS, HexManAst.UnaryOps.TILDE -> {
      compilerCheck(operand.type is IntType, expr) {
        "${expr.op} op can take integer operand only ${operand.type}"
      }
    }

    HexManAst.UnaryOps.NEG -> {
      compilerCheck(operand.type == BooleanType, expr) {
        "! op can take boolean operand only"
      }
      check(operand.expr is HExpr)
      return if (operand.expr is BooleanConst) {
        ReaderCompiler2.ExprResult(operand.stmts, BooleanConst(!operand.expr.value), BooleanType)
      } else {
        val id = nextVarId()
        ReaderCompiler2.ExprResult(
          operand.stmts + NegationOpStmt(id, operand.expr),
          HVar(id),
          operand.type,
        )
      }
    }
  }

  return if (operand.expr is IntConst) {
    val value = when (operand.type) {
    #for (typ in valTypes)
      #{typ.typeName} -> when (expr.op) {
        HexManAst.UnaryOps.MINUS -> #{typ.createFunc(`-(operand.expr as #{typ.constName})`)}
        HexManAst.UnaryOps.PLUS -> #{typ.createFunc(`+(operand.expr as #{typ.constName})`)}
        HexManAst.UnaryOps.TILDE -> #{typ.createFunc(`(operand.expr as #{typ.constName}).inv()`)}
        HexManAst.UnaryOps.NEG -> throw AssertionError()
      }
    #end
    }
    ReaderCompiler2.ExprResult(operand.stmts, value, operand.type)
  } else {
    check(operand.expr is HExpr)
    val id = nextVarId()
    ReaderCompiler2.ExprResult(
      operand.stmts + UnaryIntOpStmt(id, expr.op, operand.expr),
      HVar(id),
      operand.type,
    )
  }
}
