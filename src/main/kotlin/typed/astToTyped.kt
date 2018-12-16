package com.danstutzman.kotlinc.typed

import com.danstutzman.kotlinc.AccessFlags
import com.danstutzman.kotlinc.ast.Ast
import com.danstutzman.kotlinc.ast.Call
import com.danstutzman.kotlinc.ast.FileContents
import com.danstutzman.kotlinc.ast.FunDec
import com.danstutzman.kotlinc.ast.parseSourceToAst
import com.danstutzman.kotlinc.ast.Plus
import com.danstutzman.kotlinc.ast.Sequence
import com.danstutzman.kotlinc.ast.StringConstant
import com.danstutzman.kotlinc.typed.Type

public fun convertClassToTyped(className: String, fileContents: FileContents): Class {
  val _constructor_ = Method(
    "<init>", listOf<Type>(), AccessFlags(public=true), Type.VoidType,
      Expr.InvokeSpecial(
        "java/lang/Object", Expr.SuperInConstructor,
        "<init>", "()V", Type.VoidType)
    )
  return Class(
    className,
    "java/lang/Object",
    listOf(convertFunDecToMethod(fileContents.child as FunDec))
  )
}

fun convertFunDecToMethod(funDec: FunDec): Method {
  val paramTypes = listOf<Type>() //listOf(Type.Array(Type.StringType))
  val returnExpr = convertExpr(funDec.returnExpr)
  return Method(
    funDec.name,
    paramTypes,
    AccessFlags(public=true, static=true),
    returnExpr.getType(),
    returnExpr
  )
}

fun convertExpr(expr: Ast): Expr {
  if (expr is Call) {
    if (expr.methodName == "println") {
      return Expr.InvokeVirtual(
        "java/io/PrintStream",
        Expr.Field(
          "java/lang/System", "out", "Ljava/io/PrintStream;", Type.VoidType),
        "println", "(Ljava/lang/Object;)V",
        Type.VoidType,
        listOf(convertExpr(expr.arg0))
      )
    } else {
      throw RuntimeException("Unknown methodName ${expr.methodName}")
    }
  } else if (expr is StringConstant) {
    return Expr.ConstantString(expr.s)
  } else if (expr is Sequence) {
    return Expr.Sequence(expr.exprs.map { convertExpr(it) })
  } else if (expr is Plus) {
    // TODO: Use StringBuilder that gets <init> called instead of making another
    val appendChild1 = Expr.InvokeVirtual(
      "java/lang/StringBuilder",
      Expr.New("java/lang/StringBuilder"),
      "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
      Type.VoidType,
      listOf(convertExpr(expr.child1))
    )
    val appendChild2 = Expr.InvokeVirtual(
      "java/lang/StringBuilder",
      appendChild1,
      "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
      Type.VoidType,
      listOf(convertExpr(expr.child2))
    )
    val toString = Expr.InvokeVirtual(
      "java/lang/StringBuilder",
      appendChild2,
      "toString", "()Ljava/lang/String;",
      Type.StringType,
      listOf<Expr>()
    )
    return Expr.Sequence(listOf(
      Expr.New("java/lang/StringBuilder"),
      toString
    ))
  } else {
    throw RuntimeException("Unknown Ast ${expr::class}")
  }
}
