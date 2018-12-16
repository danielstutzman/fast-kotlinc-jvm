package com.danstutzman.kotlinc.asm

import com.danstutzman.kotlinc.AccessFlags
import com.danstutzman.kotlinc.Type
import com.danstutzman.kotlinc.ast.FileContents
import com.danstutzman.kotlinc.ast.FunDec
import com.danstutzman.kotlinc.ast.StringConstant
import com.danstutzman.kotlinc.typed.Class
import com.danstutzman.kotlinc.typed.convertClassToTyped
import com.danstutzman.kotlinc.typed.Expr
import com.danstutzman.kotlinc.typed.Method
import kotlin.test.assertEquals
import org.junit.Test

class AstToTypedTest {
  @Test fun astToTyped() {
    val ast = FileContents(FunDec("f2", StringConstant("abc")))
    val typed = Class("F2Kt", "java/lang/Object", listOf(
      Method(
        "f2", listOf<Type>(), AccessFlags(public=true, static=true),
        Type.StringType, Expr.ConstantString("abc")
      )
    ))
    assertEquals(typed, convertClassToTyped("F2Kt", ast))
  }
}
