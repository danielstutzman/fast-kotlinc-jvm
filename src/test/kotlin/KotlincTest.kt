package com.danstutzman.kotlinc.tests

import com.danstutzman.kotlinc.Ast
import com.danstutzman.kotlinc.StringConstant
import com.danstutzman.kotlinc.sourceToBytecode
import com.danstutzman.kotlinc.ToAstVisitor
import com.danstutzman.kotlinc.decompile.ClassPrinter
import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import java.io.File
import java.io.FileInputStream
import kotlin.test.assertEquals
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import org.objectweb.asm.ClassReader

fun toAst(source: String): Ast {
  val kotlinLexer = KotlinLexer(CharStreams.fromString(source))
  val commonTokenStream = CommonTokenStream(kotlinLexer)
  val kotlinParser = KotlinParser(commonTokenStream)
  val tree = kotlinParser.expression()
  val visitor = ToAstVisitor()
  return visitor.visit(tree)
}

class KotlincTest {
  @Test fun stringLiteral(): Unit {
		assertEquals(StringConstant("abc"), toAst("\"abc\""))
  }

  @Test fun helloWorld() {
    val decompiler2 = ClassPrinter()
    ClassReader(FileInputStream("fixtures/output-expected/HelloKt.class")).accept(decompiler2, 0)
    val expectedDecompiled = decompiler2.getDecompiledClass()

    val actualBytecode = sourceToBytecode("hello.kt",
      File("fixtures/input/hello.kt").readText())
    val decompiler = ClassPrinter()
    ClassReader(actualBytecode).accept(decompiler, 0)
    val actualDecompiled = decompiler.getDecompiledClass()

    println(expectedDecompiled)
    println(actualDecompiled)
    assertEquals(expectedDecompiled.toString(), actualDecompiled.toString())
  }
}
