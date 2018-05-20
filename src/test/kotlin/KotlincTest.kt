package com.danstutzman.kotlinc.tests

import com.danstutzman.kotlinc.Ast
import com.danstutzman.kotlinc.StringConstant
import com.danstutzman.kotlinc.ToAstVisitor
import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import kotlin.test.assertEquals
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test

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
}
