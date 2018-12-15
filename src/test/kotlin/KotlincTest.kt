package com.danstutzman.kotlinc.tests

import com.danstutzman.kotlinc.Ast
import com.danstutzman.kotlinc.StringConstant
import com.danstutzman.kotlinc.sourceToBytecode
import com.danstutzman.kotlinc.ToAstVisitor
import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Method
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

fun runKotlin(path: String, methodName: String): Any? {
  val source = File(path).readText()
  val bytecode = sourceToBytecode(path.split("/").last(), source)
  val class_ = loadClass(null, bytecode)
  val method = class_.getMethod(methodName) //Array<String>::class.java)
  return method.invoke(null) //arrayOf<String>())
}

class KotlincTest {
  @Test fun stringLiteral(): Unit {
		assertEquals(StringConstant("abc"), toAst("\"abc\""))
  }

  @Test fun helloWorld() {
    runKotlin("fixtures/input/hello.kt", "main")
    assertEquals(null, runKotlin("fixtures/input/f1.kt", "f1"))
    assertEquals("abc", runKotlin("fixtures/input/f2.kt", "f2"))
  }
}
