package com.danstutzman.kotlinc.ast

import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

fun printSourceTree(node: ParseTree, indentation: Int) {
  val className = node::class.java.name
  val shortName: String =
    if (className.contains("$")) className.split("$")[1]
    else if (node is org.antlr.v4.runtime.tree.TerminalNodeImpl)
      node.getPayload().toString()
    else className
  println(" ".repeat(indentation) + shortName)
  for (i in 0..node.getChildCount() - 1) {
    printSourceTree(node.getChild(i), indentation + 1)
  }
}

fun parseSourceToAst(source: String): FileContents {
  val kotlinLexer = KotlinLexer(CharStreams.fromString(source))
  val commonTokenStream = CommonTokenStream(kotlinLexer)
  val kotlinParser = KotlinParser(commonTokenStream)
  val tree = kotlinParser.kotlinFile()
  printSourceTree(tree, 0)

  val visitor = ToAstVisitor()
  return visitor.visit(tree) as FileContents
}
