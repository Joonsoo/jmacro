package com.giyeok.jmacro

import com.giyeok.jparser.ktlib.*

class JMacroAst(
  val source: String,
  val history: List<KernelSet>,
  val idIssuer: IdIssuer = IdIssuerImpl(0)
) {
  private fun nextId(): Int = idIssuer.nextId()

  sealed interface AstNode {
    val nodeId: Int
    val start: Int
    val end: Int
  }

data class TupleExpr(
  val elems: List<Expr>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Primary, AstNode

data class LetDirective(
  val name: Name,
  val type: Type?,
  val value: Expr,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroDirective, AstNode

data class ForDirective(
  val iterName: ExtractPattern,
  val iterOver: Expr,
  val body: MacroBody,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroDirective, AstNode

sealed interface MacroElem: AstNode

data class NameReplace(
  val name: Name,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): InnerSnipMacroDirective, MacroDirective, AstNode

data class TupleType(
  val elems: List<Type>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Type, AstNode

sealed interface Callee: Expr, AstNode

data class FuncType(
  val params: List<Type>,
  val result: Type,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Type, AstNode

data class InnerSnipEscape(
  val value: String,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): SnipElem, AstNode

data class ExtractTuple(
  val elems: List<Name>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): ExtractPattern, AstNode

data class Paren(
  val body: Expr,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Primary, AstNode

data class PlainSnip(
  val snip: String,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroElem, AstNode

data class Name(
  val name: String,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Callee, ExtractPattern, LambdaParam, Primary, Type, AstNode

data class MacroDecl(
  val name: Name,
  val params: List<Param>,
  val body: MacroBody,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroDirective, AstNode

data class TypedParam(
  val name: Name,
  val type: Type,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): LambdaParam, AstNode

data class Lambda(
  val params: List<LambdaParam>,
  val body: Expr,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Primary, AstNode

sealed interface LambdaParam: AstNode

data class ExprReplace(
  val expr: Expr,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): InnerSnipMacroDirective, MacroDirective, AstNode

sealed interface Expr: AstNode

sealed interface SnipElem: AstNode

data class ArrayExpr(
  val elems: List<Expr>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Primary, AstNode

data class SharpEscape(

  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroElem, AstNode

data class Param(
  val name: Name,
  val type: Type?,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): AstNode

data class ArrayType(
  val elem: Type,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Type, AstNode

sealed interface InnerSnipMacroDirective: SnipElem, AstNode

data class SnipExpr(
  val elems: List<SnipElem>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Primary, AstNode

sealed interface Primary: CallOr, AstNode

data class ClassElem(
  val name: Name,
  val body: Expr,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): AstNode

sealed interface MacroDirective: MacroElem, AstNode

sealed interface CallOr: Expr, AstNode

sealed interface ExtractPattern: AstNode

data class ClassDecl(
  val name: Name,
  val fields: List<Param>,
  val elems: List<ClassElem>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroDirective, AstNode

data class Call(
  val callee: Callee,
  val args: List<Expr>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): CallOr, AstNode

data class InnerSnipElem(
  val snip: String,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): SnipElem, AstNode

data class MacroBody(
  val elems: List<MacroElem>,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): AstNode

sealed interface Type: AstNode

data class MemberAccess(
  val value: Primary,
  val name: Name,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): Callee, Primary, AstNode


fun matchStart(): MacroBody {
  val lastGen = source.length
  val kernel = history[lastGen].getSingle(2, 1, 0, lastGen)
  return matchMacroBody(kernel.beginGen, kernel.endGen)
}

fun matchMacroBody(beginGen: Int, endGen: Int): MacroBody {
val var1 = unrollRepeat0(history, 3, 6, 4, 5, beginGen, endGen).map { k ->
val var2 = matchMacroElem(k.first, k.second)
var2
}
val var3 = MacroBody(var1, nextId(), beginGen, endGen)
return var3
}

fun matchMacroElem(beginGen: Int, endGen: Int): MacroElem {
val var4 = history[endGen].findByBeginGenOpt(7, 1, beginGen)
val var5 = history[endGen].findByBeginGenOpt(15, 1, beginGen)
val var6 = history[endGen].findByBeginGenOpt(18, 1, beginGen)
check(hasSingleTrue(var4 != null, var5 != null, var6 != null))
val var7 = when {
var4 != null -> {
val var8 = matchPlainSnip(beginGen, endGen)
var8
}
var5 != null -> {
val var9 = matchSharpEscape(beginGen, endGen)
var9
}
else -> {
val var10 = matchMacroDirective(beginGen, endGen)
var10
}
}
return var7
}

fun matchPlainSnip(beginGen: Int, endGen: Int): PlainSnip {
val var11 = unrollRepeat1(history, 9, 10, 10, 14, beginGen, endGen).map { k ->
source[k.first]
}
val var12 = PlainSnip(var11.joinToString("") { it.toString() }, nextId(), beginGen, endGen)
return var12
}

fun matchSharpEscape(beginGen: Int, endGen: Int): SharpEscape {
val var13 = SharpEscape(nextId(), beginGen, endGen)
return var13
}

fun matchMacroDirective(beginGen: Int, endGen: Int): MacroDirective {
val var14 = history[endGen].findByBeginGenOpt(19, 1, beginGen)
val var15 = history[endGen].findByBeginGenOpt(160, 1, beginGen)
val var16 = history[endGen].findByBeginGenOpt(162, 1, beginGen)
val var17 = history[endGen].findByBeginGenOpt(211, 1, beginGen)
val var18 = history[endGen].findByBeginGenOpt(218, 1, beginGen)
val var19 = history[endGen].findByBeginGenOpt(237, 1, beginGen)
check(hasSingleTrue(var14 != null, var15 != null, var16 != null, var17 != null, var18 != null, var19 != null))
val var20 = when {
var14 != null -> {
val var21 = matchClassDecl(beginGen, endGen)
var21
}
var15 != null -> {
val var22 = matchMacroNameReplace(beginGen, endGen)
var22
}
var16 != null -> {
val var23 = matchExprReplace(beginGen, endGen)
var23
}
var17 != null -> {
val var24 = matchMacroDecl(beginGen, endGen)
var24
}
var18 != null -> {
val var25 = matchForDirective(beginGen, endGen)
var25
}
else -> {
val var26 = matchLetDirective(beginGen, endGen)
var26
}
}
return var20
}

fun matchLetDirective(beginGen: Int, endGen: Int): LetDirective {
val var27 = getSequenceElems(history, 238, listOf(239,27,63,94,27,139,27,140), beginGen, endGen)
val var28 = matchName(var27[2].first, var27[2].second)
val var29 = history[var27[3].second].findByBeginGenOpt(62, 1, var27[3].first)
val var30 = history[var27[3].second].findByBeginGenOpt(95, 1, var27[3].first)
check(hasSingleTrue(var29 != null, var30 != null))
val var31 = when {
var29 != null -> null
else -> {
val var32 = getSequenceElems(history, 96, listOf(27,97,27,98), var27[3].first, var27[3].second)
val var33 = matchType(var32[3].first, var32[3].second)
var33
}
}
val var34 = matchExpr(var27[7].first, var27[7].second)
val var35 = LetDirective(var28, var31, var34, nextId(), beginGen, endGen)
return var35
}

fun matchName(beginGen: Int, endGen: Int): Name {
val var36 = getSequenceElems(history, 68, listOf(69,70), beginGen, endGen)
val var37 = unrollRepeat0(history, 70, 72, 4, 71, var36[1].first, var36[1].second).map { k ->
source[k.first]
}
val var38 = Name(source[var36[0].first].toString() + var37.joinToString("") { it.toString() }, nextId(), beginGen, endGen)
return var38
}

fun matchMacroDecl(beginGen: Int, endGen: Int): MacroDecl {
val var39 = getSequenceElems(history, 212, listOf(213,27,63,27,89,2,216), beginGen, endGen)
val var40 = matchName(var39[2].first, var39[2].second)
val var41 = matchParams(var39[4].first, var39[4].second)
val var42 = matchMacroBody(var39[5].first, var39[5].second)
val var43 = MacroDecl(var40, var41, var42, nextId(), beginGen, endGen)
return var43
}

fun matchExprReplace(beginGen: Int, endGen: Int): ExprReplace {
val var44 = getSequenceElems(history, 163, listOf(13,133,27,140,27,164), beginGen, endGen)
val var45 = matchExpr(var44[3].first, var44[3].second)
val var46 = ExprReplace(var45, nextId(), beginGen, endGen)
return var46
}

fun matchMacroNameReplace(beginGen: Int, endGen: Int): NameReplace {
val var47 = getSequenceElems(history, 161, listOf(13,63), beginGen, endGen)
val var48 = matchName(var47[1].first, var47[1].second)
val var49 = NameReplace(var48, nextId(), beginGen, endGen)
return var49
}

fun matchForDirective(beginGen: Int, endGen: Int): ForDirective {
val var50 = getSequenceElems(history, 219, listOf(220,27,91,27,222,27,228,27,140,27,109,2,216), beginGen, endGen)
val var51 = matchExtractPattern(var50[4].first, var50[4].second)
val var52 = matchExpr(var50[8].first, var50[8].second)
val var53 = matchMacroBody(var50[11].first, var50[11].second)
val var54 = ForDirective(var51, var52, var53, nextId(), beginGen, endGen)
return var54
}

fun matchExpr(beginGen: Int, endGen: Int): Expr {
val var55 = matchCallOr(beginGen, endGen)
return var55
}

fun matchParams(beginGen: Int, endGen: Int): List<Param> {
val var56 = getSequenceElems(history, 90, listOf(91,27,92,124,27,109), beginGen, endGen)
val var57 = matchParam(var56[2].first, var56[2].second)
val var58 = unrollRepeat0(history, 124, 126, 4, 125, var56[3].first, var56[3].second).map { k ->
val var59 = getSequenceElems(history, 127, listOf(27,108,27,92), k.first, k.second)
val var60 = matchParam(var59[3].first, var59[3].second)
var60
}
return listOf(var57) + var58
}

fun matchParam(beginGen: Int, endGen: Int): Param {
val var61 = getSequenceElems(history, 93, listOf(63,94), beginGen, endGen)
val var62 = matchName(var61[0].first, var61[0].second)
val var63 = history[var61[1].second].findByBeginGenOpt(62, 1, var61[1].first)
val var64 = history[var61[1].second].findByBeginGenOpt(95, 1, var61[1].first)
check(hasSingleTrue(var63 != null, var64 != null))
val var65 = when {
var63 != null -> null
else -> {
val var66 = getSequenceElems(history, 96, listOf(27,97,27,98), var61[1].first, var61[1].second)
val var67 = matchType(var66[3].first, var66[3].second)
var67
}
}
val var68 = Param(var62, var65, nextId(), beginGen, endGen)
return var68
}

fun matchClassDecl(beginGen: Int, endGen: Int): ClassDecl {
val var69 = getSequenceElems(history, 20, listOf(21,27,63,27,89,128,133,134,27,164), beginGen, endGen)
val var70 = matchName(var69[2].first, var69[2].second)
val var71 = matchParams(var69[4].first, var69[4].second)
val var73 = history[var69[7].second].findByBeginGenOpt(62, 1, var69[7].first)
val var74 = history[var69[7].second].findByBeginGenOpt(135, 1, var69[7].first)
check(hasSingleTrue(var73 != null, var74 != null))
val var75 = when {
var73 != null -> null
else -> {
val var76 = getSequenceElems(history, 136, listOf(27,137,203), var69[7].first, var69[7].second)
val var77 = matchClassElem(var76[1].first, var76[1].second)
val var78 = unrollRepeat0(history, 203, 205, 4, 204, var76[2].first, var76[2].second).map { k ->
val var79 = getSequenceElems(history, 206, listOf(207,137), k.first, k.second)
val var80 = matchClassElem(var79[1].first, var79[1].second)
var80
}
listOf(var77) + var78
}
}
val var72 = var75
val var81 = ClassDecl(var70, var71, (var72 ?: listOf()), nextId(), beginGen, endGen)
return var81
}

fun matchClassElem(beginGen: Int, endGen: Int): ClassElem {
val var82 = getSequenceElems(history, 138, listOf(63,27,139,27,140), beginGen, endGen)
val var83 = matchName(var82[0].first, var82[0].second)
val var84 = matchExpr(var82[4].first, var82[4].second)
val var85 = ClassElem(var83, var84, nextId(), beginGen, endGen)
return var85
}

fun matchType(beginGen: Int, endGen: Int): Type {
val var86 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var87 = history[endGen].findByBeginGenOpt(99, 1, beginGen)
val var88 = history[endGen].findByBeginGenOpt(114, 1, beginGen)
val var89 = history[endGen].findByBeginGenOpt(120, 1, beginGen)
check(hasSingleTrue(var86 != null, var87 != null, var88 != null, var89 != null))
val var90 = when {
var86 != null -> {
val var91 = matchName(beginGen, endGen)
var91
}
var87 != null -> {
val var92 = matchFuncType(beginGen, endGen)
var92
}
var88 != null -> {
val var93 = matchArrayType(beginGen, endGen)
var93
}
else -> {
val var94 = matchTupleType(beginGen, endGen)
var94
}
}
return var90
}

fun matchArrayType(beginGen: Int, endGen: Int): ArrayType {
val var95 = getSequenceElems(history, 115, listOf(98,27,116), beginGen, endGen)
val var96 = matchType(var95[0].first, var95[0].second)
val var97 = ArrayType(var96, nextId(), beginGen, endGen)
return var97
}

fun matchTupleType(beginGen: Int, endGen: Int): TupleType {
val var98 = getSequenceElems(history, 121, listOf(91,27,98,122,27,109), beginGen, endGen)
val var99 = matchType(var98[2].first, var98[2].second)
val var100 = unrollRepeat1(history, 122, 106, 106, 123, var98[3].first, var98[3].second).map { k ->
val var101 = getSequenceElems(history, 107, listOf(27,108,27,98), k.first, k.second)
val var102 = matchType(var101[3].first, var101[3].second)
var102
}
val var103 = TupleType(listOf(var99) + var100, nextId(), beginGen, endGen)
return var103
}

fun matchFuncType(beginGen: Int, endGen: Int): FuncType {
val var105 = getSequenceElems(history, 100, listOf(91,101,27,109,27,110,27,98), beginGen, endGen)
val var106 = history[var105[1].second].findByBeginGenOpt(62, 1, var105[1].first)
val var107 = history[var105[1].second].findByBeginGenOpt(102, 1, var105[1].first)
check(hasSingleTrue(var106 != null, var107 != null))
val var108 = when {
var106 != null -> null
else -> {
val var109 = getSequenceElems(history, 103, listOf(27,98,104), var105[1].first, var105[1].second)
val var110 = matchType(var109[1].first, var109[1].second)
val var111 = unrollRepeat0(history, 104, 106, 4, 105, var109[2].first, var109[2].second).map { k ->
val var112 = getSequenceElems(history, 107, listOf(27,108,27,98), k.first, k.second)
val var113 = matchType(var112[3].first, var112[3].second)
var113
}
listOf(var110) + var111
}
}
val var104 = var108
val var114 = matchType(var105[7].first, var105[7].second)
val var115 = FuncType((var104 ?: listOf()), var114, nextId(), beginGen, endGen)
return var115
}

fun matchCallOr(beginGen: Int, endGen: Int): CallOr {
val var116 = history[endGen].findByBeginGenOpt(142, 1, beginGen)
val var117 = history[endGen].findByBeginGenOpt(196, 6, beginGen)
check(hasSingleTrue(var116 != null, var117 != null))
val var118 = when {
var116 != null -> {
val var119 = matchPrimary(beginGen, endGen)
var119
}
else -> {
val var120 = getSequenceElems(history, 196, listOf(197,27,91,198,27,109), beginGen, endGen)
val var121 = matchCallee(var120[0].first, var120[0].second)
val var123 = history[var120[3].second].findByBeginGenOpt(62, 1, var120[3].first)
val var124 = history[var120[3].second].findByBeginGenOpt(199, 1, var120[3].first)
check(hasSingleTrue(var123 != null, var124 != null))
val var125 = when {
var123 != null -> null
else -> {
val var126 = getSequenceElems(history, 200, listOf(27,201), var120[3].first, var120[3].second)
val var127 = matchArgs(var126[1].first, var126[1].second)
var127
}
}
val var122 = var125
val var128 = Call(var121, (var122 ?: listOf()), nextId(), beginGen, endGen)
var128
}
}
return var118
}

fun matchPrimary(beginGen: Int, endGen: Int): Primary {
val var129 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var130 = history[endGen].findByBeginGenOpt(143, 1, beginGen)
val var131 = history[endGen].findByBeginGenOpt(165, 1, beginGen)
val var132 = history[endGen].findByBeginGenOpt(176, 1, beginGen)
val var133 = history[endGen].findByBeginGenOpt(188, 1, beginGen)
val var134 = history[endGen].findByBeginGenOpt(192, 5, beginGen)
val var135 = history[endGen].findByBeginGenOpt(193, 1, beginGen)
check(hasSingleTrue(var129 != null, var130 != null, var131 != null, var132 != null, var133 != null, var134 != null, var135 != null))
val var136 = when {
var129 != null -> {
val var137 = matchName(beginGen, endGen)
var137
}
var130 != null -> {
val var138 = matchSnipExpr(beginGen, endGen)
var138
}
var131 != null -> {
val var139 = matchLambda(beginGen, endGen)
var139
}
var132 != null -> {
val var140 = matchArrayExpr(beginGen, endGen)
var140
}
var133 != null -> {
val var141 = matchTupleExpr(beginGen, endGen)
var141
}
var134 != null -> {
val var142 = getSequenceElems(history, 192, listOf(91,27,140,27,109), beginGen, endGen)
val var143 = matchExpr(var142[2].first, var142[2].second)
val var144 = Paren(var143, nextId(), beginGen, endGen)
var144
}
else -> {
val var145 = matchMemberAccess(beginGen, endGen)
var145
}
}
return var136
}

fun matchTupleExpr(beginGen: Int, endGen: Int): TupleExpr {
val var146 = getSequenceElems(history, 189, listOf(91,27,140,190,27,109), beginGen, endGen)
val var147 = matchExpr(var146[2].first, var146[2].second)
val var148 = unrollRepeat1(history, 190, 183, 183, 191, var146[3].first, var146[3].second).map { k ->
val var149 = getSequenceElems(history, 184, listOf(27,108,27,140), k.first, k.second)
val var150 = matchExpr(var149[3].first, var149[3].second)
var150
}
val var151 = TupleExpr(listOf(var147) + var148, nextId(), beginGen, endGen)
return var151
}

fun matchLambda(beginGen: Int, endGen: Int): Lambda {
val var153 = getSequenceElems(history, 166, listOf(91,167,27,109,27,110,27,140), beginGen, endGen)
val var154 = history[var153[1].second].findByBeginGenOpt(62, 1, var153[1].first)
val var155 = history[var153[1].second].findByBeginGenOpt(168, 1, var153[1].first)
check(hasSingleTrue(var154 != null, var155 != null))
val var156 = when {
var154 != null -> null
else -> {
val var157 = getSequenceElems(history, 169, listOf(27,170,172), var153[1].first, var153[1].second)
val var158 = matchLambdaParam(var157[1].first, var157[1].second)
val var159 = unrollRepeat0(history, 172, 174, 4, 173, var157[2].first, var157[2].second).map { k ->
val var160 = getSequenceElems(history, 175, listOf(27,108,27,170), k.first, k.second)
val var161 = matchLambdaParam(var160[3].first, var160[3].second)
var161
}
listOf(var158) + var159
}
}
val var152 = var156
val var162 = matchExpr(var153[7].first, var153[7].second)
val var163 = Lambda((var152 ?: listOf()), var162, nextId(), beginGen, endGen)
return var163
}

fun matchLambdaParam(beginGen: Int, endGen: Int): LambdaParam {
val var164 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var165 = history[endGen].findByBeginGenOpt(171, 5, beginGen)
check(hasSingleTrue(var164 != null, var165 != null))
val var166 = when {
var164 != null -> {
val var167 = matchName(beginGen, endGen)
var167
}
else -> {
val var168 = getSequenceElems(history, 171, listOf(63,27,97,27,98), beginGen, endGen)
val var169 = matchName(var168[0].first, var168[0].second)
val var170 = matchType(var168[4].first, var168[4].second)
val var171 = TypedParam(var169, var170, nextId(), beginGen, endGen)
var171
}
}
return var166
}

fun matchArrayExpr(beginGen: Int, endGen: Int): ArrayExpr {
val var173 = getSequenceElems(history, 177, listOf(118,178,27,119), beginGen, endGen)
val var174 = history[var173[1].second].findByBeginGenOpt(62, 1, var173[1].first)
val var175 = history[var173[1].second].findByBeginGenOpt(179, 1, var173[1].first)
check(hasSingleTrue(var174 != null, var175 != null))
val var176 = when {
var174 != null -> null
else -> {
val var177 = getSequenceElems(history, 180, listOf(27,140,181,185), var173[1].first, var173[1].second)
val var178 = matchExpr(var177[1].first, var177[1].second)
val var179 = unrollRepeat0(history, 181, 183, 4, 182, var177[2].first, var177[2].second).map { k ->
val var180 = getSequenceElems(history, 184, listOf(27,108,27,140), k.first, k.second)
val var181 = matchExpr(var180[3].first, var180[3].second)
var181
}
listOf(var178) + var179
}
}
val var172 = var176
val var182 = ArrayExpr((var172 ?: listOf()), nextId(), beginGen, endGen)
return var182
}

fun matchSnipExpr(beginGen: Int, endGen: Int): SnipExpr {
val var183 = getSequenceElems(history, 144, listOf(145,146,145), beginGen, endGen)
val var184 = unrollRepeat0(history, 146, 148, 4, 147, var183[1].first, var183[1].second).map { k ->
val var185 = matchSnipElem(k.first, k.second)
var185
}
val var186 = SnipExpr(var184, nextId(), beginGen, endGen)
return var186
}

fun matchSnipElem(beginGen: Int, endGen: Int): SnipElem {
val var187 = history[endGen].findByBeginGenOpt(149, 1, beginGen)
val var188 = history[endGen].findByBeginGenOpt(156, 1, beginGen)
val var189 = history[endGen].findByBeginGenOpt(159, 1, beginGen)
check(hasSingleTrue(var187 != null, var188 != null, var189 != null))
val var190 = when {
var187 != null -> {
val var191 = matchInnerSnipElem(beginGen, endGen)
var191
}
var188 != null -> {
val var192 = matchInnerSnipEscape(beginGen, endGen)
var192
}
else -> {
val var193 = matchInnerSnipMacroDirective(beginGen, endGen)
var193
}
}
return var190
}

fun matchInnerSnipEscape(beginGen: Int, endGen: Int): InnerSnipEscape {
val var194 = history[endGen].findByBeginGenOpt(16, 1, beginGen)
val var195 = history[endGen].findByBeginGenOpt(157, 1, beginGen)
check(hasSingleTrue(var194 != null, var195 != null))
val var196 = when {
var194 != null -> {
val var197 = InnerSnipEscape("#", nextId(), beginGen, endGen)
var197
}
else -> {
val var198 = InnerSnipEscape("`", nextId(), beginGen, endGen)
var198
}
}
return var196
}

fun matchInnerSnipMacroDirective(beginGen: Int, endGen: Int): InnerSnipMacroDirective {
val var199 = history[endGen].findByBeginGenOpt(160, 1, beginGen)
val var200 = history[endGen].findByBeginGenOpt(162, 1, beginGen)
check(hasSingleTrue(var199 != null, var200 != null))
val var201 = when {
var199 != null -> {
val var202 = matchMacroNameReplace(beginGen, endGen)
var202
}
else -> {
val var203 = matchExprReplace(beginGen, endGen)
var203
}
}
return var201
}

fun matchExtractPattern(beginGen: Int, endGen: Int): ExtractPattern {
val var204 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var205 = history[endGen].findByBeginGenOpt(223, 6, beginGen)
check(hasSingleTrue(var204 != null, var205 != null))
val var206 = when {
var204 != null -> {
val var207 = matchName(beginGen, endGen)
var207
}
else -> {
val var208 = getSequenceElems(history, 223, listOf(91,27,63,224,27,109), beginGen, endGen)
val var209 = matchName(var208[2].first, var208[2].second)
val var210 = unrollRepeat0(history, 224, 226, 4, 225, var208[3].first, var208[3].second).map { k ->
val var211 = getSequenceElems(history, 227, listOf(27,108,27,63), k.first, k.second)
val var212 = matchName(var211[3].first, var211[3].second)
var212
}
val var213 = ExtractTuple(listOf(var209) + var210, nextId(), beginGen, endGen)
var213
}
}
return var206
}

fun matchCallee(beginGen: Int, endGen: Int): Callee {
val var214 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var215 = history[endGen].findByBeginGenOpt(193, 1, beginGen)
check(hasSingleTrue(var214 != null, var215 != null))
val var216 = when {
var214 != null -> {
val var217 = matchName(beginGen, endGen)
var217
}
else -> {
val var218 = matchMemberAccess(beginGen, endGen)
var218
}
}
return var216
}

fun matchArgs(beginGen: Int, endGen: Int): List<Expr> {
val var219 = getSequenceElems(history, 202, listOf(140,181), beginGen, endGen)
val var220 = matchExpr(var219[0].first, var219[0].second)
val var221 = unrollRepeat0(history, 181, 183, 4, 182, var219[1].first, var219[1].second).map { k ->
val var222 = getSequenceElems(history, 184, listOf(27,108,27,140), k.first, k.second)
val var223 = matchExpr(var222[3].first, var222[3].second)
var223
}
return listOf(var220) + var221
}

fun matchInnerSnipElem(beginGen: Int, endGen: Int): InnerSnipElem {
val var224 = unrollRepeat1(history, 151, 152, 152, 155, beginGen, endGen).map { k ->
source[k.first]
}
val var225 = InnerSnipElem(var224.joinToString("") { it.toString() }, nextId(), beginGen, endGen)
return var225
}

fun matchMemberAccess(beginGen: Int, endGen: Int): MemberAccess {
val var226 = getSequenceElems(history, 194, listOf(142,27,195,27,63), beginGen, endGen)
val var227 = matchPrimary(var226[0].first, var226[0].second)
val var228 = matchName(var226[4].first, var226[4].second)
val var229 = MemberAccess(var227, var228, nextId(), beginGen, endGen)
return var229
}

}
