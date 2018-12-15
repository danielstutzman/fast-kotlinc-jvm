package com.danstutzman.kotlinc.tests

import com.danstutzman.kotlinc.Ast
import com.danstutzman.kotlinc.astToBytecode
import com.danstutzman.kotlinc.FileContents
import com.danstutzman.kotlinc.FunDec
import com.danstutzman.kotlinc.Plus
import com.danstutzman.kotlinc.StringConstant
import com.danstutzman.kotlinc.sourceToBytecode
import com.danstutzman.kotlinc.ToAstVisitor
import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import org.objectweb.asm.ClassReader

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

fun toAst(source: String): Ast {
  val kotlinLexer = KotlinLexer(CharStreams.fromString(source))
  val commonTokenStream = CommonTokenStream(kotlinLexer)
  val kotlinParser = KotlinParser(commonTokenStream)
  val tree = kotlinParser.expression()
  val visitor = ToAstVisitor()
  return visitor.visit(tree)
}

fun loadClass(className: String?, b: ByteArray): Class<*> {
	val loader = ClassLoader.getSystemClassLoader()
	val cls = Class.forName("java.lang.ClassLoader")
	val method = cls.getDeclaredMethod(
		"defineClass", 
		String::class.java,
		ByteArray::class.java,
		Int::class.java,
		Int::class.java
	)

	method.setAccessible(true)
  val clazz = method.invoke(loader, className, b, Integer(0), Integer(b.size))
		as Class<*>
	method.setAccessible(false)

	return clazz
}

fun printTime() {
  println("TIME: ${LocalDateTime.now().format(formatter)}")
}

fun runKotlin(path: String, methodName: String): Any? {
  val source = File(path).readText()
  val bytecode = sourceToBytecode(path.split("/").last(), source)
  val class_ = loadClass(null, bytecode)
  val method = class_.getMethod(methodName) //Array<String>::class.java)
  return method.invoke(null) //arrayOf<String>())
}

fun runKotlinAst(path: String, methodName: String, ast: FileContents): Any? {
  printTime()
  println("astToBytecode...")
  val bytecode = astToBytecode(path.split("/").last(), ast)
  printTime()
  println("invoking...")
  val class_ = loadClass(null, bytecode)
  val method = class_.getMethod(methodName) //Array<String>::class.java)
  return method.invoke(null) //arrayOf<String>())
}

class KotlincTest {
  @Test fun stringLiteral(): Unit {
		assertEquals(StringConstant("abc"), toAst("\"abc\""))
  }

  @Test fun helloWorld() {
    if (true) {
      runKotlin("fixtures/input/hello.kt", "main")
      printTime()
      assertEquals(null, runKotlin("fixtures/input/f1.kt", "f1"))
      printTime()
      assertEquals("abc", runKotlin("fixtures/input/f2.kt", "f2"))
      printTime()
      assertEquals("abcdef", runKotlin("fixtures/input/f3.kt", "f3"))
      printTime()
      assertEquals("abc", runKotlin("fixtures/input/f4.kt", "f4"))
      printTime()
    } else {
      printTime()
      assertEquals("abc",
        runKotlinAst("fixtures/input/f2.kt", "f2", FileContents(
          FunDec("f2", StringConstant("abc"))
        )))
      printTime()
      assertEquals("abcdef",
        runKotlinAst("fixtures/input/f3.kt", "f3", FileContents(
          FunDec("f3", Plus(StringConstant("abc"), StringConstant("def")))
        )))
      printTime()
    }
  }
}
