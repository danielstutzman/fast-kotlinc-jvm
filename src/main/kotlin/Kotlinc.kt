package com.danstutzman.kotlinc

import com.danstutzman.kotlinc.ast.Ast
import com.danstutzman.kotlinc.ast.Call
import com.danstutzman.kotlinc.ast.FileContents
import com.danstutzman.kotlinc.ast.FunDec
import com.danstutzman.kotlinc.ast.parseSourceToAst
import com.danstutzman.kotlinc.ast.Plus
import com.danstutzman.kotlinc.ast.Sequence
import com.danstutzman.kotlinc.ast.StringConstant
import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

fun main(args: Array<String>) {
  if (args.size != 2) {
    System.err.println("""Usage:
      Arg 1: path to .kt input
      Arg 2: path to .class output
      """.trimIndent())
    System.exit(1)
  }
  val inputPath = File(args[0])
  val outputPath = File(args[1])
  val source = inputPath.readText()
  val fileContents = parseSourceToAst(source)
  val bytecode =
    astToBytecode(filenameToClassName(inputPath.getName()), fileContents)

  FileOutputStream(outputPath).use { stream ->
    stream.write(bytecode)
  }
  println(outputPath)
}

fun printTime() {
  println("TIME: ${LocalDateTime.now().format(formatter)}")
}

fun astToBytecode(filename: String, fileContents: FileContents): ByteArray {
  val className = filenameToClassName(filename)
  val resolved = resolveClass(className, fileContents)
  println(resolved)

  val flattened = flattenClass(resolved)
  return flattened
}

fun filenameToClassName(filename: String): String =
  filename.substring(0, 1).toUpperCase() +
  filename.substring(1).replace(Regex("\\.kt$"), "Kt")

public fun resolveClass(className: String, fileContents: FileContents): Nested.Class {
  val _constructor_ = Nested.Method(
    "<init>", listOf<Type>(), AccessFlags(public=true), Type.VoidType,
      Nested.Expr.InvokeSpecial(
        "java/lang/Object", Nested.Expr.SuperInConstructor,
        "<init>", "()V", Type.VoidType)
    )
  return Nested.Class(
    className,
    "java/lang/Object",
    listOf(resolveFunDec(fileContents.child as FunDec))
  )
}

fun resolveFunDec(funDec: FunDec): Nested.Method {
  val paramTypes = listOf<Type>() //listOf(Type.Array(Type.StringType))
  val returnExpr = resolveExpr(funDec.returnExpr)
  return Nested.Method(
    funDec.name,
    paramTypes,
    AccessFlags(public=true, static=true),
    returnExpr.getType(),
    returnExpr
  )
}

fun resolveExpr(expr: Ast): Nested.Expr {
  if (expr is Call) {
    if (expr.methodName == "println") {
      return Nested.Expr.InvokeVirtual(
        "java/io/PrintStream",
        Nested.Expr.Field(
          "java/lang/System", "out", "Ljava/io/PrintStream;", Type.VoidType),
        "println", "(Ljava/lang/Object;)V",
        Type.VoidType,
        listOf(resolveExpr(expr.arg0))
      )
    } else {
      throw RuntimeException("Unknown methodName ${expr.methodName}")
    }
  } else if (expr is StringConstant) {
    return Nested.Expr.ConstantString(expr.s)
  } else if (expr is Sequence) {
    return Nested.Expr.Sequence(expr.exprs.map { resolveExpr(it) })
  } else if (expr is Plus) {
		// TODO: Use StringBuilder that gets <init> called instead of making another
		val appendChild1 = Nested.Expr.InvokeVirtual(
			"java/lang/StringBuilder",
			Nested.Expr.New("java/lang/StringBuilder"),
			"append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
			Type.VoidType,
			listOf(resolveExpr(expr.child1))
		)
		val appendChild2 = Nested.Expr.InvokeVirtual(
			"java/lang/StringBuilder",
			appendChild1,
			"append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
			Type.VoidType,
			listOf(resolveExpr(expr.child2))
		)
		val toString = Nested.Expr.InvokeVirtual(
			"java/lang/StringBuilder",
			appendChild2,
			"toString", "()Ljava/lang/String;",
			Type.StringType,
			listOf<Nested.Expr>()
		)
    return Nested.Expr.Sequence(listOf(
			Nested.Expr.New("java/lang/StringBuilder"),
			toString
		))
  } else {
    throw RuntimeException("Unknown Ast ${expr::class}")
  }
}
