package kotlinc

import com.github.sarahbuisson.kotlinparser.KotlinLexer
import com.github.sarahbuisson.kotlinparser.KotlinParser
import java.io.File
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
  if (args.size == 0) {
    System.err.println("Usage: First arg should be path to .kt file")
    System.exit(1)
  }
  val sourcePath = File(args[0])

  val kotlinLexer = KotlinLexer(CharStreams.fromString(sourcePath.readText()))
  val commonTokenStream = CommonTokenStream(kotlinLexer)
  val kotlinParser = KotlinParser(commonTokenStream)
  val tree = kotlinParser.kotlinFile()
  val visitor = ToAstVisitor()
  println("visit results: ${visitor.visit(tree)}")
}
