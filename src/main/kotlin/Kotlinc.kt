package com.danstutzman.kotlinc

import com.danstutzman.kotlinc.asm.convertClassToAsm
import com.danstutzman.kotlinc.asm.serializeClass
import com.danstutzman.kotlinc.ast.Ast
import com.danstutzman.kotlinc.ast.Call
import com.danstutzman.kotlinc.ast.FileContents
import com.danstutzman.kotlinc.ast.FunDec
import com.danstutzman.kotlinc.ast.parseSourceToAst
import com.danstutzman.kotlinc.ast.Plus
import com.danstutzman.kotlinc.ast.Sequence
import com.danstutzman.kotlinc.ast.StringConstant
import com.danstutzman.kotlinc.typed.convertClassToTyped
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
  val typed = convertClassToTyped(className, fileContents)
  println("typed: ${typed}")
  val asm = convertClassToAsm(typed)
  println("asm: ${asm}")
  val bytecode = serializeClass(asm)
  return bytecode
}

fun filenameToClassName(filename: String): String =
  filename.substring(0, 1).toUpperCase() +
  filename.substring(1).replace(Regex("\\.kt$"), "Kt")
