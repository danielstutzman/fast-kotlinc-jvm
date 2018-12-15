package com.danstutzman.kotlinc.asm

import com.danstutzman.kotlinc.AccessFlags
import com.danstutzman.kotlinc.Nested
import com.danstutzman.kotlinc.Type
import kotlin.test.assertEquals
import org.junit.Test

class flattenedToAsmTest {
  @Test fun nestedToAsm() {
    val nested = Nested.Class("F2Kt", "java/lang/Object", listOf(
      Nested.Method(
        "f2", listOf<Type>(), AccessFlags(public=true, static=true),
        Type.StringType, Nested.Expr.ConstantString("abc")
      )
    ))
    val asm = Class("F2Kt", "java/lang/Object", listOf(
      Method("f2", "()Ljava/lang/String;", listOf(
        I9n.LdcString("abc"),
        I9n.Areturn
      ))
    ))
    assertEquals(asm, convertClassToAsm(nested))
  }
}
