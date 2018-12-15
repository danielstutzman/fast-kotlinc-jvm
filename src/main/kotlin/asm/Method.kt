package com.danstutzman.kotlinc.asm

data class Method(
  val name: String,
  val descriptor: String,
  val i9ns: List<I9n>
)
