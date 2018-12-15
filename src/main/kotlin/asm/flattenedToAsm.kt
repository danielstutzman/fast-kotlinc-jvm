package com.danstutzman.kotlinc.asm

import com.danstutzman.kotlinc.Nested
import com.danstutzman.kotlinc.Type
import com.danstutzman.kotlinc.asm.I9n.Aload0
import com.danstutzman.kotlinc.asm.I9n.Areturn
import com.danstutzman.kotlinc.asm.I9n.Dup
import com.danstutzman.kotlinc.asm.I9n.Getstatic
import com.danstutzman.kotlinc.asm.I9n.Invokespecial
import com.danstutzman.kotlinc.asm.I9n.Invokevirtual
import com.danstutzman.kotlinc.asm.I9n.LdcClasspath
import com.danstutzman.kotlinc.asm.I9n.LdcString
import com.danstutzman.kotlinc.asm.I9n.New
import com.danstutzman.kotlinc.asm.I9n.Return

fun convertClass(c: Nested.Class): Class =
  Class(c.name, c.parentPath, c.methods.map { convertMethod(it) })

fun convertMethod(m: Nested.Method): Method {
  val descriptor = "(" +
    m.paramTypes.map { it.toDescriptor() }.joinToString("") +
    ")" + m.returnType.toDescriptor()
  val bodyI9ns = convertExpr(m.returnExpr)
  val returnI9n = when (m.returnExpr.getType()) {
    Type.VoidType -> Return
    Type.StringType -> Areturn
    else -> throw RuntimeException(
      "Can't return with type ${m.returnExpr.getType()}")
  }
  return Method(descriptor, bodyI9ns + returnI9n)
}

fun convertExpr(e: Nested.Expr): List<I9n> =
  when (e) {
    is Nested.Expr.Class ->
      listOf(LdcClasspath(e.classPath))
    is Nested.Expr.ConstantString ->
      listOf(LdcString(e.string))
    is Nested.Expr.SuperInConstructor ->
      listOf(Aload0)
    is Nested.Expr.Field ->
      listOf(Getstatic(e.classPath, e.fieldName, e.fieldType))
    is Nested.Expr.InvokeSpecial ->
      listOf(Invokespecial(e.classPath, e.methodName, e.methodType))
    is Nested.Expr.InvokeVirtual ->
      listOf(Invokevirtual(e.classPath, e.methodName, e.methodType))
    is Nested.Expr.Sequence ->
      e.exprs.flatMap { convertExpr(it) }
    is Nested.Expr.New ->
      listOf(New(e.classPath), Dup, Invokespecial(e.classPath, "<init>", "()V"))
    // else -> throw RuntimeException("Unknown expr ${e::class}")
  }
