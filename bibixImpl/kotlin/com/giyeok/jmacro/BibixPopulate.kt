package com.giyeok.jmacro

import com.giyeok.bibix.base.BibixValue
import com.giyeok.bibix.base.BuildContext
import com.giyeok.bibix.base.FileValue
import com.giyeok.bibix.base.ListValue
import com.giyeok.bibix.base.StringValue
import com.giyeok.bibix.base.TupleValue
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

class BibixPopulate {
  fun build(context: BuildContext): BibixValue {
    val file = context.getFileField("file")
    val src = file.readText()
    val outname = context.getNullableStringArg("outname")
    val valuesArgs = context.getNullableArgOf<ListValue>("values")

    val values = valuesArgs?.values?.associate { pair ->
      (pair as TupleValue)
      val (key, value) = pair.values
      (key as StringValue).value to SnipValue((value as StringValue).value)
    } ?: mapOf()

    val parsed = JMacroParser.parse(src)

    val builder = StringBuilder()
    JMacro(builder).write(parsed, MacroCtx(MacroCtx.findClassDefs(parsed), values))
    val outfile = outname ?: file.name

    val outpath = context.clearDestDirectory().resolve(outfile)
    outpath.writeText(builder.toString())

    return FileValue(outpath)
  }
}
