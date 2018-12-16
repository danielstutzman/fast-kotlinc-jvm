package com.danstutzman.kotlinc.asm

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
import com.danstutzman.kotlinc.typed.Class as TypedClass
import com.danstutzman.kotlinc.typed.Expr
import com.danstutzman.kotlinc.typed.Method as TypedMethod
import com.danstutzman.kotlinc.typed.Type

fun convertClassToAsm(c: TypedClass): Class =
  Class(c.name, c.parentPath, c.methods.map { convertMethod(it) })

fun convertMethod(m: TypedMethod): Method {
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
  return Method(m.name, descriptor, bodyI9ns + returnI9n)
}

fun convertExpr(e: Expr): List<I9n> =
  when (e) {
    is Expr.Class ->
      listOf(LdcClasspath(e.classPath))
    is Expr.ConstantString ->
      listOf(LdcString(e.string))
    is Expr.SuperInConstructor ->
      listOf(Aload0)
    is Expr.Field ->
      listOf(Getstatic(e.classPath, e.fieldName, e.fieldType))
    is Expr.InvokeSpecial ->
      listOf(Invokespecial(e.classPath, e.methodName, e.methodType))
    is Expr.InvokeVirtual ->
      convertExpr(e.objectExpr) +
      e.args.flatMap { convertExpr(it) } +
      listOf(Invokevirtual(e.classPath, e.methodName, e.methodType))
    is Expr.Sequence ->
      e.exprs.flatMap { convertExpr(it) }
    is Expr.New ->
      listOf(New(e.classPath), Dup, Invokespecial(e.classPath, "<init>", "()V"))
    is Expr.AppendString ->
      convertAppendString(e)
    // else -> throw RuntimeException("Unknown expr ${e::class}")
  }

fun convertAppendString(e: Expr.AppendString): List<I9n> =
  listOf(
    I9n.New("java/lang/StringBuilder"),
    I9n.Dup,
    I9n.Invokespecial("java/lang/StringBuilder", "<init>", "()V")
  ) +
  convertExpr(e.expr1) +
  listOf(
    I9n.Invokevirtual("java/lang/StringBuilder", "append",
      "(Ljava/lang/String;)Ljava/lang/StringBuilder;")
  ) +
  convertExpr(e.expr2) +
  listOf(
    I9n.Invokevirtual("java/lang/StringBuilder", "append",
      "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
    I9n.Invokevirtual("java/lang/StringBuilder", "toString",
      "()Ljava/lang/String;")
  )
