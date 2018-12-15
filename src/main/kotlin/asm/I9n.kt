package com.danstutzman.kotlinc.asm

sealed class I9n {
  object Aload0: I9n()
  object Areturn: I9n()
  object Dup: I9n()
  data class Getstatic(
    val classPath: String,
    val fieldName: String,
    val fieldType: String): I9n()
  data class Invokespecial(
    val classPath: String,
    val methodName: String,
    val methodType: String): I9n()
  data class Invokevirtual(
    val classPath: String,
    val methodName: String,
    val methodType: String): I9n()
  data class LdcClasspath(
    val classPath: String): I9n()
  data class LdcString(
    val string: String): I9n()
  data class New(
    val classPath: String): I9n()
  object Return: I9n()
}
