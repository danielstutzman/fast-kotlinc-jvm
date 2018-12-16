package com.danstutzman.kotlinc.tests

import com.danstutzman.kotlinc.asm.convertClassToAsm
import com.danstutzman.kotlinc.asm.serializeClass
import com.danstutzman.kotlinc.ast.FileContents
import com.danstutzman.kotlinc.ast.FunDec
import com.danstutzman.kotlinc.ast.parseSourceToAst
import com.danstutzman.kotlinc.ast.StringConstant
import com.danstutzman.kotlinc.AccessFlags
import com.danstutzman.kotlinc.astToBytecode
import com.danstutzman.kotlinc.filenameToClassName
import com.danstutzman.kotlinc.Nested
import com.danstutzman.kotlinc.resolveClass
import com.danstutzman.kotlinc.Type
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
  val fileContents = parseSourceToAst(source)
  val bytecode = astToBytecode(
    filenameToClassName(path.split("/").last()), fileContents)
  val class_ = loadClass(null, bytecode)
  val method = class_.getMethod(methodName) //Array<String>::class.java)
  return method.invoke(null) //arrayOf<String>())
}

class KotlincTest {
  @Test fun helloWorld() {
    runKotlin("fixtures/input/hello.kt", "main")
    assertEquals(null, runKotlin("fixtures/input/f1.kt", "f1"))
    assertEquals("abc", runKotlin("fixtures/input/f2.kt", "f2"))
    assertEquals("abcdef", runKotlin("fixtures/input/f3.kt", "f3"))
    assertEquals("abc", runKotlin("fixtures/input/f4.kt", "f4"))
  }

  @Test fun astToNested() {
    val ast = FileContents(FunDec("f2", StringConstant("abc")))
    val nested = Nested.Class("F2Kt", "java/lang/Object", listOf(
			Nested.Method(
				"f2", listOf<Type>(), AccessFlags(public=true, static=true),
				Type.StringType, Nested.Expr.ConstantString("abc")
			)
		))
    assertEquals(nested, resolveClass("F2Kt", ast))
	}

  @Test fun integrationTest() {
    val ast = FileContents(FunDec("f5", StringConstant("abc")))
    val nested = resolveClass("F5Kt", ast)
    val asm = convertClassToAsm(nested)
    val bytecode = serializeClass(asm)
    // File("F5Kt.class").writeBytes(bytecode)
    val class_ = loadClass(null, bytecode)
    val method = class_.getMethod("f5")
    val returned = method.invoke(arrayOf<String>())
    assertEquals("abc", returned)
  }
}
