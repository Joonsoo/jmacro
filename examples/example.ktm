#class ValType(name, extractFunc: (Snip) -> Snip, createFunc: (Snip) -> Snip) {
  typeName = `#{name}Type`
  constName = `#{name}Const`
}
#let valTypes: ValType[] = [
  ValType(`I8`, (x) -> `#x.value.byteValueExact()`, (x) -> `I8Const((#x).toByte())`),
  ValType(`I16`, (x) -> `#x.value.shortValueExact()`, (x) -> `I16Const((#x).toShort())`),
  ValType(`I32`, (x) -> `#x.value.intValueExact()`, (x) -> `I32Const((#x).toInt())`),
  ValType(`I64`, (x) -> `#x.value.longValueExact()`, (x) -> `I64Const((#x).toLong())`),
  ValType(`U8`, (x) -> `#x.value.ubyteValueExact()`, (x) -> `U8Const((#x).toByte().toUByte())`),
  ValType(`U16`, (x) -> `#x.value.ushortValueExact()`, (x) -> `U16Const((#x).toShort().toUShort())`),
  ValType(`U32`, (x) -> `#x.value.uintValueExact()`, (x) -> `U32Const((#x).toInt().toUInt())`),
  ValType(`U64`, (x) -> `#x.value.ulongValueExact()`, (x) -> `U64Const((#x).toLong().toULong())`),
]
when (expr.op) {
#for ((i, ifunc) in [(`HexManAst.AddOps.ADD`, (a, b) -> `#a + #b`)])
  #i -> when (rhs.type) {
  #for (j in valTypes)
    #{j.typeName} -> #{j.createFunc(ifunc(j.extractFunc(`lhs.expr`), j.extractFunc(`(rhs.expr as #{j.typeName})`)))}
  #end
  }
#end
}



val value = when (expr.op) {
#for ((op, opfunc) in [(`HexManAst.AddOps.ADD`, (a, b) -> `#a + #b`), (`HexManAst.AddOps.SUB`, (a, b) -> `#a - #b`)])
#op -> when(rhs.type) {
  #for (typ in valTypes)
  #{typ.typeName} -> #{typ.createFunc(opfunc(typ.extractFunc(`lhs.expr`), typ.extractFunc(`(rhs.expr as #{typ.typeName})`)))}
  #end
}
#end
}


#a#a#a
