package com.danstutzman.kotlinc

import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

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

  val kotlinLexer = KotlinLexer(CharStreams.fromString(inputPath.readText()))
  val commonTokenStream = CommonTokenStream(kotlinLexer)
  val kotlinParser = KotlinParser(commonTokenStream)
  val tree = kotlinParser.kotlinFile()
  val visitor = ToAstVisitor()
  val fileContents = visitor.visit(tree) as FileContents
  val className = filenameToClassName(inputPath.getName())
  val resolved = resolveClass(className, fileContents)
  val bytecode = flattenClass(resolved)

  DataOutputStream(FileOutputStream(outputPath)).use { stream ->
    bytecode.write(stream)
  }
  println(outputPath)
}

fun filenameToClassName(filename: String): String =
  filename.substring(0, 1).toUpperCase() +
  filename.substring(1).replace("\\.kt$", "Kt")

fun resolveClass(className: String, fileContents: FileContents): Nested.Class {
  val constructor_ = Nested.Method(
    "<init>", listOf<Type>(), AccessFlags(public=true),
      Nested.Expr.InvokeSpecial(
        "java/lang/Object", Nested.Expr.SuperInConstructor,
        "<init>", "()V")
    )
  return Nested.Class(
    className,
    "java/lang/Object",
    listOf(constructor_, resolveFunDec(fileContents.child as FunDec))
  )
}

fun resolveFunDec(funDec: FunDec): Nested.Method {
  val paramTypes = listOf(Type.Array(Type.StringType))
  return Nested.Method(
    funDec.name,
    paramTypes,
    AccessFlags(public=true, static=true),
    resolveExpr(funDec.returnExpr)
  )
}

fun resolveExpr(expr: Ast): Nested.Expr {
  if (expr is Call) {
    if (expr.methodName == "println") {
      return Nested.Expr.InvokeVirtual(
        "java/io/PrintStream",
        Nested.Expr.Field(
          "java/lang/System", "out", "Ljava/io/PrintStream;"),
        "println", "(Ljava/lang/String;)V",
        listOf(resolveExpr(expr.arg0))
      )
    } else {
      throw RuntimeException("Unknown methodName ${expr.methodName}")
    }
  } else if (expr is StringConstant) {
    return Nested.Expr.ConstantString(expr.s)
  } else if (expr is Sequence) {
    return Nested.Expr.Sequence(expr.exprs.map { resolveExpr(it) })
  } else {
    throw RuntimeException("Unknown Ast ${expr::class}")
  }
}
