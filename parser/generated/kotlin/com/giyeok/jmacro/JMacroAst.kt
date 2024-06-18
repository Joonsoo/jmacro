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

data class TypedParam(
  val name: Name,
  val type: Type,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): LambdaParam, AstNode

data class ForDirective(
  val iterName: ExtractPattern,
  val iterOver: Expr,
  val body: MacroBody,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroDirective, AstNode

data class SharpEscape(

  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): MacroElem, AstNode

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

data class ClassParam(
  val name: Name,
  val type: Type?,
  override val nodeId: Int,
  override val start: Int,
  override val end: Int,
): AstNode

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
  val fields: List<ClassParam>,
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
val var15 = history[endGen].findByBeginGenOpt(158, 1, beginGen)
val var16 = history[endGen].findByBeginGenOpt(160, 1, beginGen)
val var17 = history[endGen].findByBeginGenOpt(216, 1, beginGen)
val var18 = history[endGen].findByBeginGenOpt(237, 1, beginGen)
check(hasSingleTrue(var14 != null, var15 != null, var16 != null, var17 != null, var18 != null))
val var19 = when {
var14 != null -> {
val var20 = matchClassDecl(beginGen, endGen)
var20
}
var15 != null -> {
val var21 = matchMacroNameReplace(beginGen, endGen)
var21
}
var16 != null -> {
val var22 = matchExprReplace(beginGen, endGen)
var22
}
var17 != null -> {
val var23 = matchForDirective(beginGen, endGen)
var23
}
else -> {
val var24 = matchLetDirective(beginGen, endGen)
var24
}
}
return var19
}

fun matchLetDirective(beginGen: Int, endGen: Int): LetDirective {
val var25 = getSequenceElems(history, 238, listOf(239,27,63,92,27,137,27,138,209), beginGen, endGen)
val var26 = matchName(var25[2].first, var25[2].second)
val var27 = history[var25[3].second].findByBeginGenOpt(62, 1, var25[3].first)
val var28 = history[var25[3].second].findByBeginGenOpt(93, 1, var25[3].first)
check(hasSingleTrue(var27 != null, var28 != null))
val var29 = when {
var27 != null -> null
else -> {
val var30 = getSequenceElems(history, 94, listOf(27,95,27,96), var25[3].first, var25[3].second)
val var31 = matchType(var30[3].first, var30[3].second)
var31
}
}
val var32 = matchExpr(var25[7].first, var25[7].second)
val var33 = LetDirective(var26, var29, var32, nextId(), beginGen, endGen)
return var33
}

fun matchName(beginGen: Int, endGen: Int): Name {
val var34 = getSequenceElems(history, 68, listOf(69,70), beginGen, endGen)
val var35 = unrollRepeat0(history, 70, 72, 4, 71, var34[1].first, var34[1].second).map { k ->
source[k.first]
}
val var36 = Name(source[var34[0].first].toString() + var35.joinToString("") { it.toString() }, nextId(), beginGen, endGen)
return var36
}

fun matchExprReplace(beginGen: Int, endGen: Int): ExprReplace {
val var37 = getSequenceElems(history, 161, listOf(13,131,27,138,27,162), beginGen, endGen)
val var38 = matchExpr(var37[3].first, var37[3].second)
val var39 = ExprReplace(var38, nextId(), beginGen, endGen)
return var39
}

fun matchMacroNameReplace(beginGen: Int, endGen: Int): NameReplace {
val var40 = getSequenceElems(history, 159, listOf(13,63), beginGen, endGen)
val var41 = matchName(var40[1].first, var40[1].second)
val var42 = NameReplace(var41, nextId(), beginGen, endGen)
return var42
}

fun matchForDirective(beginGen: Int, endGen: Int): ForDirective {
val var43 = getSequenceElems(history, 217, listOf(218,27,89,27,220,27,226,27,138,27,107,2,235,209), beginGen, endGen)
val var44 = matchExtractPattern(var43[4].first, var43[4].second)
val var45 = matchExpr(var43[8].first, var43[8].second)
val var46 = matchMacroBody(var43[11].first, var43[11].second)
val var47 = ForDirective(var44, var45, var46, nextId(), beginGen, endGen)
return var47
}

fun matchExpr(beginGen: Int, endGen: Int): Expr {
val var48 = matchCallOr(beginGen, endGen)
return var48
}

fun matchClassDecl(beginGen: Int, endGen: Int): ClassDecl {
val var49 = getSequenceElems(history, 20, listOf(21,27,63,27,89,27,90,122,27,107,126,131,132,27,162,209), beginGen, endGen)
val var50 = matchName(var49[2].first, var49[2].second)
val var51 = matchClassParam(var49[6].first, var49[6].second)
val var52 = unrollRepeat0(history, 122, 124, 4, 123, var49[7].first, var49[7].second).map { k ->
val var53 = getSequenceElems(history, 125, listOf(27,106,27,90), k.first, k.second)
val var54 = matchClassParam(var53[3].first, var53[3].second)
var54
}
val var56 = history[var49[12].second].findByBeginGenOpt(62, 1, var49[12].first)
val var57 = history[var49[12].second].findByBeginGenOpt(133, 1, var49[12].first)
check(hasSingleTrue(var56 != null, var57 != null))
val var58 = when {
var56 != null -> null
else -> {
val var59 = getSequenceElems(history, 134, listOf(27,135,201), var49[12].first, var49[12].second)
val var60 = matchClassElem(var59[1].first, var59[1].second)
val var61 = unrollRepeat0(history, 201, 203, 4, 202, var59[2].first, var59[2].second).map { k ->
val var62 = getSequenceElems(history, 204, listOf(205,135), k.first, k.second)
val var63 = matchClassElem(var62[1].first, var62[1].second)
var63
}
listOf(var60) + var61
}
}
val var55 = var58
val var64 = ClassDecl(var50, listOf(var51) + var52, (var55 ?: listOf()), nextId(), beginGen, endGen)
return var64
}

fun matchClassElem(beginGen: Int, endGen: Int): ClassElem {
val var65 = getSequenceElems(history, 136, listOf(63,27,137,27,138), beginGen, endGen)
val var66 = matchName(var65[0].first, var65[0].second)
val var67 = matchExpr(var65[4].first, var65[4].second)
val var68 = ClassElem(var66, var67, nextId(), beginGen, endGen)
return var68
}

fun matchClassParam(beginGen: Int, endGen: Int): ClassParam {
val var69 = getSequenceElems(history, 91, listOf(63,92), beginGen, endGen)
val var70 = matchName(var69[0].first, var69[0].second)
val var71 = history[var69[1].second].findByBeginGenOpt(62, 1, var69[1].first)
val var72 = history[var69[1].second].findByBeginGenOpt(93, 1, var69[1].first)
check(hasSingleTrue(var71 != null, var72 != null))
val var73 = when {
var71 != null -> null
else -> {
val var74 = getSequenceElems(history, 94, listOf(27,95,27,96), var69[1].first, var69[1].second)
val var75 = matchType(var74[3].first, var74[3].second)
var75
}
}
val var76 = ClassParam(var70, var73, nextId(), beginGen, endGen)
return var76
}

fun matchType(beginGen: Int, endGen: Int): Type {
val var77 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var78 = history[endGen].findByBeginGenOpt(97, 1, beginGen)
val var79 = history[endGen].findByBeginGenOpt(112, 1, beginGen)
val var80 = history[endGen].findByBeginGenOpt(118, 1, beginGen)
check(hasSingleTrue(var77 != null, var78 != null, var79 != null, var80 != null))
val var81 = when {
var77 != null -> {
val var82 = matchName(beginGen, endGen)
var82
}
var78 != null -> {
val var83 = matchFuncType(beginGen, endGen)
var83
}
var79 != null -> {
val var84 = matchArrayType(beginGen, endGen)
var84
}
else -> {
val var85 = matchTupleType(beginGen, endGen)
var85
}
}
return var81
}

fun matchArrayType(beginGen: Int, endGen: Int): ArrayType {
val var86 = getSequenceElems(history, 113, listOf(96,27,114), beginGen, endGen)
val var87 = matchType(var86[0].first, var86[0].second)
val var88 = ArrayType(var87, nextId(), beginGen, endGen)
return var88
}

fun matchTupleType(beginGen: Int, endGen: Int): TupleType {
val var89 = getSequenceElems(history, 119, listOf(89,27,96,120,27,107), beginGen, endGen)
val var90 = matchType(var89[2].first, var89[2].second)
val var91 = unrollRepeat1(history, 120, 104, 104, 121, var89[3].first, var89[3].second).map { k ->
val var92 = getSequenceElems(history, 105, listOf(27,106,27,96), k.first, k.second)
val var93 = matchType(var92[3].first, var92[3].second)
var93
}
val var94 = TupleType(listOf(var90) + var91, nextId(), beginGen, endGen)
return var94
}

fun matchFuncType(beginGen: Int, endGen: Int): FuncType {
val var96 = getSequenceElems(history, 98, listOf(89,99,27,107,27,108,27,96), beginGen, endGen)
val var97 = history[var96[1].second].findByBeginGenOpt(62, 1, var96[1].first)
val var98 = history[var96[1].second].findByBeginGenOpt(100, 1, var96[1].first)
check(hasSingleTrue(var97 != null, var98 != null))
val var99 = when {
var97 != null -> null
else -> {
val var100 = getSequenceElems(history, 101, listOf(27,96,102), var96[1].first, var96[1].second)
val var101 = matchType(var100[1].first, var100[1].second)
val var102 = unrollRepeat0(history, 102, 104, 4, 103, var100[2].first, var100[2].second).map { k ->
val var103 = getSequenceElems(history, 105, listOf(27,106,27,96), k.first, k.second)
val var104 = matchType(var103[3].first, var103[3].second)
var104
}
listOf(var101) + var102
}
}
val var95 = var99
val var105 = matchType(var96[7].first, var96[7].second)
val var106 = FuncType((var95 ?: listOf()), var105, nextId(), beginGen, endGen)
return var106
}

fun matchCallOr(beginGen: Int, endGen: Int): CallOr {
val var107 = history[endGen].findByBeginGenOpt(140, 1, beginGen)
val var108 = history[endGen].findByBeginGenOpt(194, 6, beginGen)
check(hasSingleTrue(var107 != null, var108 != null))
val var109 = when {
var107 != null -> {
val var110 = matchPrimary(beginGen, endGen)
var110
}
else -> {
val var111 = getSequenceElems(history, 194, listOf(195,27,89,196,27,107), beginGen, endGen)
val var112 = matchCallee(var111[0].first, var111[0].second)
val var114 = history[var111[3].second].findByBeginGenOpt(62, 1, var111[3].first)
val var115 = history[var111[3].second].findByBeginGenOpt(197, 1, var111[3].first)
check(hasSingleTrue(var114 != null, var115 != null))
val var116 = when {
var114 != null -> null
else -> {
val var117 = getSequenceElems(history, 198, listOf(27,199), var111[3].first, var111[3].second)
val var118 = matchArgs(var117[1].first, var117[1].second)
var118
}
}
val var113 = var116
val var119 = Call(var112, (var113 ?: listOf()), nextId(), beginGen, endGen)
var119
}
}
return var109
}

fun matchPrimary(beginGen: Int, endGen: Int): Primary {
val var120 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var121 = history[endGen].findByBeginGenOpt(141, 1, beginGen)
val var122 = history[endGen].findByBeginGenOpt(163, 1, beginGen)
val var123 = history[endGen].findByBeginGenOpt(174, 1, beginGen)
val var124 = history[endGen].findByBeginGenOpt(186, 1, beginGen)
val var125 = history[endGen].findByBeginGenOpt(190, 5, beginGen)
val var126 = history[endGen].findByBeginGenOpt(191, 1, beginGen)
check(hasSingleTrue(var120 != null, var121 != null, var122 != null, var123 != null, var124 != null, var125 != null, var126 != null))
val var127 = when {
var120 != null -> {
val var128 = matchName(beginGen, endGen)
var128
}
var121 != null -> {
val var129 = matchSnipExpr(beginGen, endGen)
var129
}
var122 != null -> {
val var130 = matchLambda(beginGen, endGen)
var130
}
var123 != null -> {
val var131 = matchArrayExpr(beginGen, endGen)
var131
}
var124 != null -> {
val var132 = matchTupleExpr(beginGen, endGen)
var132
}
var125 != null -> {
val var133 = getSequenceElems(history, 190, listOf(89,27,138,27,107), beginGen, endGen)
val var134 = matchExpr(var133[2].first, var133[2].second)
val var135 = Paren(var134, nextId(), beginGen, endGen)
var135
}
else -> {
val var136 = matchMemberAccess(beginGen, endGen)
var136
}
}
return var127
}

fun matchTupleExpr(beginGen: Int, endGen: Int): TupleExpr {
val var137 = getSequenceElems(history, 187, listOf(89,27,138,188,27,107), beginGen, endGen)
val var138 = matchExpr(var137[2].first, var137[2].second)
val var139 = unrollRepeat1(history, 188, 181, 181, 189, var137[3].first, var137[3].second).map { k ->
val var140 = getSequenceElems(history, 182, listOf(27,106,27,138), k.first, k.second)
val var141 = matchExpr(var140[3].first, var140[3].second)
var141
}
val var142 = TupleExpr(listOf(var138) + var139, nextId(), beginGen, endGen)
return var142
}

fun matchLambda(beginGen: Int, endGen: Int): Lambda {
val var144 = getSequenceElems(history, 164, listOf(89,165,27,107,27,108,27,138), beginGen, endGen)
val var145 = history[var144[1].second].findByBeginGenOpt(62, 1, var144[1].first)
val var146 = history[var144[1].second].findByBeginGenOpt(166, 1, var144[1].first)
check(hasSingleTrue(var145 != null, var146 != null))
val var147 = when {
var145 != null -> null
else -> {
val var148 = getSequenceElems(history, 167, listOf(27,168,170), var144[1].first, var144[1].second)
val var149 = matchLambdaParam(var148[1].first, var148[1].second)
val var150 = unrollRepeat0(history, 170, 172, 4, 171, var148[2].first, var148[2].second).map { k ->
val var151 = getSequenceElems(history, 173, listOf(27,106,27,168), k.first, k.second)
val var152 = matchLambdaParam(var151[3].first, var151[3].second)
var152
}
listOf(var149) + var150
}
}
val var143 = var147
val var153 = matchExpr(var144[7].first, var144[7].second)
val var154 = Lambda((var143 ?: listOf()), var153, nextId(), beginGen, endGen)
return var154
}

fun matchLambdaParam(beginGen: Int, endGen: Int): LambdaParam {
val var155 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var156 = history[endGen].findByBeginGenOpt(169, 5, beginGen)
check(hasSingleTrue(var155 != null, var156 != null))
val var157 = when {
var155 != null -> {
val var158 = matchName(beginGen, endGen)
var158
}
else -> {
val var159 = getSequenceElems(history, 169, listOf(63,27,95,27,96), beginGen, endGen)
val var160 = matchName(var159[0].first, var159[0].second)
val var161 = matchType(var159[4].first, var159[4].second)
val var162 = TypedParam(var160, var161, nextId(), beginGen, endGen)
var162
}
}
return var157
}

fun matchArrayExpr(beginGen: Int, endGen: Int): ArrayExpr {
val var164 = getSequenceElems(history, 175, listOf(116,176,27,117), beginGen, endGen)
val var165 = history[var164[1].second].findByBeginGenOpt(62, 1, var164[1].first)
val var166 = history[var164[1].second].findByBeginGenOpt(177, 1, var164[1].first)
check(hasSingleTrue(var165 != null, var166 != null))
val var167 = when {
var165 != null -> null
else -> {
val var168 = getSequenceElems(history, 178, listOf(27,138,179,183), var164[1].first, var164[1].second)
val var169 = matchExpr(var168[1].first, var168[1].second)
val var170 = unrollRepeat0(history, 179, 181, 4, 180, var168[2].first, var168[2].second).map { k ->
val var171 = getSequenceElems(history, 182, listOf(27,106,27,138), k.first, k.second)
val var172 = matchExpr(var171[3].first, var171[3].second)
var172
}
listOf(var169) + var170
}
}
val var163 = var167
val var173 = ArrayExpr((var163 ?: listOf()), nextId(), beginGen, endGen)
return var173
}

fun matchSnipExpr(beginGen: Int, endGen: Int): SnipExpr {
val var174 = getSequenceElems(history, 142, listOf(143,144,143), beginGen, endGen)
val var175 = unrollRepeat0(history, 144, 146, 4, 145, var174[1].first, var174[1].second).map { k ->
val var176 = matchSnipElem(k.first, k.second)
var176
}
val var177 = SnipExpr(var175, nextId(), beginGen, endGen)
return var177
}

fun matchSnipElem(beginGen: Int, endGen: Int): SnipElem {
val var178 = history[endGen].findByBeginGenOpt(147, 1, beginGen)
val var179 = history[endGen].findByBeginGenOpt(154, 1, beginGen)
val var180 = history[endGen].findByBeginGenOpt(157, 1, beginGen)
check(hasSingleTrue(var178 != null, var179 != null, var180 != null))
val var181 = when {
var178 != null -> {
val var182 = matchInnerSnipElem(beginGen, endGen)
var182
}
var179 != null -> {
val var183 = matchInnerSnipEscape(beginGen, endGen)
var183
}
else -> {
val var184 = matchInnerSnipMacroDirective(beginGen, endGen)
var184
}
}
return var181
}

fun matchInnerSnipEscape(beginGen: Int, endGen: Int): InnerSnipEscape {
val var185 = history[endGen].findByBeginGenOpt(16, 1, beginGen)
val var186 = history[endGen].findByBeginGenOpt(155, 1, beginGen)
check(hasSingleTrue(var185 != null, var186 != null))
val var187 = when {
var185 != null -> {
val var188 = InnerSnipEscape("#", nextId(), beginGen, endGen)
var188
}
else -> {
val var189 = InnerSnipEscape("`", nextId(), beginGen, endGen)
var189
}
}
return var187
}

fun matchInnerSnipMacroDirective(beginGen: Int, endGen: Int): InnerSnipMacroDirective {
val var190 = history[endGen].findByBeginGenOpt(158, 1, beginGen)
val var191 = history[endGen].findByBeginGenOpt(160, 1, beginGen)
check(hasSingleTrue(var190 != null, var191 != null))
val var192 = when {
var190 != null -> {
val var193 = matchMacroNameReplace(beginGen, endGen)
var193
}
else -> {
val var194 = matchExprReplace(beginGen, endGen)
var194
}
}
return var192
}

fun matchExtractPattern(beginGen: Int, endGen: Int): ExtractPattern {
val var195 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var196 = history[endGen].findByBeginGenOpt(221, 6, beginGen)
check(hasSingleTrue(var195 != null, var196 != null))
val var197 = when {
var195 != null -> {
val var198 = matchName(beginGen, endGen)
var198
}
else -> {
val var199 = getSequenceElems(history, 221, listOf(89,27,63,222,27,107), beginGen, endGen)
val var200 = matchName(var199[2].first, var199[2].second)
val var201 = unrollRepeat0(history, 222, 224, 4, 223, var199[3].first, var199[3].second).map { k ->
val var202 = getSequenceElems(history, 225, listOf(27,106,27,63), k.first, k.second)
val var203 = matchName(var202[3].first, var202[3].second)
var203
}
val var204 = ExtractTuple(listOf(var200) + var201, nextId(), beginGen, endGen)
var204
}
}
return var197
}

fun matchCallee(beginGen: Int, endGen: Int): Callee {
val var205 = history[endGen].findByBeginGenOpt(63, 1, beginGen)
val var206 = history[endGen].findByBeginGenOpt(191, 1, beginGen)
check(hasSingleTrue(var205 != null, var206 != null))
val var207 = when {
var205 != null -> {
val var208 = matchName(beginGen, endGen)
var208
}
else -> {
val var209 = matchMemberAccess(beginGen, endGen)
var209
}
}
return var207
}

fun matchArgs(beginGen: Int, endGen: Int): List<Expr> {
val var210 = getSequenceElems(history, 200, listOf(138,179), beginGen, endGen)
val var211 = matchExpr(var210[0].first, var210[0].second)
val var212 = unrollRepeat0(history, 179, 181, 4, 180, var210[1].first, var210[1].second).map { k ->
val var213 = getSequenceElems(history, 182, listOf(27,106,27,138), k.first, k.second)
val var214 = matchExpr(var213[3].first, var213[3].second)
var214
}
return listOf(var211) + var212
}

fun matchInnerSnipElem(beginGen: Int, endGen: Int): InnerSnipElem {
val var215 = unrollRepeat1(history, 149, 150, 150, 153, beginGen, endGen).map { k ->
source[k.first]
}
val var216 = InnerSnipElem(var215.joinToString("") { it.toString() }, nextId(), beginGen, endGen)
return var216
}

fun matchMemberAccess(beginGen: Int, endGen: Int): MemberAccess {
val var217 = getSequenceElems(history, 192, listOf(140,27,193,27,63), beginGen, endGen)
val var218 = matchPrimary(var217[0].first, var217[0].second)
val var219 = matchName(var217[4].first, var217[4].second)
val var220 = MemberAccess(var218, var219, nextId(), beginGen, endGen)
return var220
}

}
