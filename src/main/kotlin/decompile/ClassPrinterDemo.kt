package com.danstutzman.kotlinc.decompile

import org.objectweb.asm.ClassReader

class Hello {
  fun print() = println("hello")
}

fun main(args: Array<String>) {
  ClassReader("Hello").accept(ClassPrinter(), 0)
}
